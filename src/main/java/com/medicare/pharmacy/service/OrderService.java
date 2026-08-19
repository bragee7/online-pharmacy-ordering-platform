package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.CheckoutRequest;
import com.medicare.pharmacy.dto.OrderDto;
import com.medicare.pharmacy.dto.OrderItemDto;
import com.medicare.pharmacy.entity.Cart;
import com.medicare.pharmacy.entity.CartItem;
import com.medicare.pharmacy.entity.Delivery;
import com.medicare.pharmacy.entity.Inventory;
import com.medicare.pharmacy.entity.Medicine;
import com.medicare.pharmacy.entity.Order;
import com.medicare.pharmacy.entity.OrderItem;
import com.medicare.pharmacy.entity.Payment;
import com.medicare.pharmacy.entity.Prescription;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.DeliveryStatus;
import com.medicare.pharmacy.enums.OrderStatus;
import com.medicare.pharmacy.enums.PaymentStatus;
import com.medicare.pharmacy.enums.PrescriptionStatus;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.exception.BadRequestException;
import com.medicare.pharmacy.exception.InsufficientStockException;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.CartItemRepository;
import com.medicare.pharmacy.repository.CartRepository;
import com.medicare.pharmacy.repository.DeliveryRepository;
import com.medicare.pharmacy.repository.InventoryRepository;
import com.medicare.pharmacy.repository.MedicineRepository;
import com.medicare.pharmacy.repository.OrderItemRepository;
import com.medicare.pharmacy.repository.OrderRepository;
import com.medicare.pharmacy.repository.PaymentRepository;
import com.medicare.pharmacy.repository.PrescriptionRepository;
import com.medicare.pharmacy.repository.UserRepository;
import com.medicare.pharmacy.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuditService auditService;

    @Transactional
    public OrderDto checkout(Long userId, CheckoutRequest req) {
        if (req.getShippingAddress() == null || req.getShippingAddress().isBlank()) {
            throw new BadRequestException("Shipping address is required");
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Prescription> approved = prescriptionRepository
                .findByUserIdAndStatus(userId, PrescriptionStatus.APPROVED);
        boolean hasApprovedRx = !approved.isEmpty();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Medicine med = ci.getMedicine();
            if (!med.isActive()) {
                throw new BadRequestException("Medicine not available: " + med.getName());
            }
            int available = inventoryRepository.findByMedicineId(med.getId())
                    .map(Inventory::getAvailable)
                    .orElse(0);
            if (available < ci.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for " + med.getName());
            }
            if (med.isPrescriptionRequired() && !hasApprovedRx) {
                throw new BadRequestException("Approved prescription required for " + med.getName());
            }
            total = total.add(med.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        Order order = Order.builder()
                .user(user)
                .orderNumber(OrderNumberGenerator.generate())
                .totalAmount(total)
                .status(OrderStatus.PLACED)
                .shippingAddress(req.getShippingAddress().trim())
                .build();
        Order savedOrder = orderRepository.save(order);

        for (CartItem ci : cartItems) {
            Medicine med = ci.getMedicine();
            BigDecimal unitPrice = med.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            OrderItem oi = OrderItem.builder()
                    .order(savedOrder)
                    .medicine(med)
                    .quantity(ci.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
            orderItemRepository.save(oi);
            Inventory inv = inventoryRepository.findByMedicineId(med.getId())
                    .orElseThrow(() -> new InsufficientStockException("Insufficient stock for " + med.getName()));
            inv.setQuantity(inv.getQuantity() - ci.getQuantity());
            inventoryRepository.save(inv);
        }

        String method = req.getPaymentMethod() == null || req.getPaymentMethod().isBlank()
                ? "COD"
                : req.getPaymentMethod().trim();
        boolean isCod = method.equalsIgnoreCase("COD");
        Payment payment = Payment.builder()
                .order(savedOrder)
                .amount(total)
                .paymentMethod(method)
                .status(isCod ? PaymentStatus.PENDING : PaymentStatus.SUCCESS)
                .transactionReference(OrderNumberGenerator.transactionRef())
                .paidAt(isCod ? null : LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        Delivery delivery = Delivery.builder()
                .order(savedOrder)
                .trackingNumber(OrderNumberGenerator.trackingNumber())
                .carrier("MediCare Logistics")
                .status(DeliveryStatus.PENDING)
                .estimatedDeliveryDate(LocalDate.now().plusDays(5))
                .build();
        deliveryRepository.save(delivery);

        cartItemRepository.deleteByCartId(cart.getId());

        auditService.log(userId, "CREATE_ORDER", "Order", savedOrder.getId(),
                "Checkout total=" + total + " items=" + cartItems.size());

        return toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> listForUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> listAll(OrderStatus status, Pageable pageable) {
        if (status == null) {
            return orderRepository.findAll(pageable).map(this::toDto);
        }
        return orderRepository.findByStatus(status, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        enforceAccess(order);
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDto getByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        enforceAccess(order);
        return toDto(order);
    }

    @Transactional
    public OrderDto updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot change DELIVERED/CANCELLED order");
        }
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        if (newStatus == OrderStatus.DELIVERED) {
            deliveryRepository.findByOrderId(saved.getId()).ifPresent(d -> {
                d.setStatus(DeliveryStatus.DELIVERED);
                d.setDeliveredAt(LocalDateTime.now());
                deliveryRepository.save(d);
            });
        }
        if (newStatus == OrderStatus.CANCELLED) {
            restock(saved);
            paymentRepository.findByOrderId(saved.getId()).ifPresent(p -> {
                if (p.getStatus() == PaymentStatus.SUCCESS) {
                    p.setStatus(PaymentStatus.REFUNDED);
                    paymentRepository.save(p);
                }
            });
        }
        User current = safeCurrentUser();
        auditService.log(current != null ? current.getId() : null,
                "UPDATE_ORDER_STATUS", "Order", saved.getId(), "Status -> " + newStatus);
        return toDto(saved);
    }

    @Transactional
    public OrderDto cancel(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        User current = authService.getCurrentUser();
        boolean isOwner = order.getUser().getId().equals(userId);
        boolean isStaff = current.getRole() == Role.ADMIN || current.getRole() == Role.PHARMACIST;
        if (!isOwner && !isStaff) {
            throw new AccessDeniedException("Access denied");
        }
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Only PLACED/CONFIRMED orders can be cancelled");
        }
        restock(order);
        paymentRepository.findByOrderId(order.getId()).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                p.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(p);
            }
        });
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        auditService.log(current.getId(), "CANCEL_ORDER", "Order", saved.getId(),
                "Cancelled order " + saved.getOrderNumber());
        return toDto(saved);
    }

    private void restock(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem oi : items) {
            inventoryRepository.findByMedicineId(oi.getMedicine().getId()).ifPresent(inv -> {
                inv.setQuantity(inv.getQuantity() + oi.getQuantity());
                inventoryRepository.save(inv);
            });
        }
    }

    private void enforceAccess(Order order) {
        User current = authService.getCurrentUser();
        if (current.getRole() == Role.CUSTOMER
                && !order.getUser().getId().equals(current.getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private User safeCurrentUser() {
        try {
            return authService.getCurrentUser();
        } catch (Exception ex) {
            return null;
        }
    }

    private OrderDto toDto(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDto> itemDtos = items.stream()
                .map(oi -> OrderItemDto.builder()
                        .id(oi.getId())
                        .medicineId(oi.getMedicine().getId())
                        .medicineName(oi.getMedicine().getName())
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .subtotal(oi.getSubtotal())
                        .build())
                .toList();
        String paymentStatus = paymentRepository.findByOrderId(order.getId())
                .map(p -> p.getStatus().name())
                .orElse(PaymentStatus.PENDING.name());
        String tracking = deliveryRepository.findByOrderId(order.getId())
                .map(Delivery::getTrackingNumber)
                .orElse(null);
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .userName(order.getUser().getName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDtos)
                .paymentStatus(paymentStatus)
                .trackingNumber(tracking)
                .build();
    }
}
