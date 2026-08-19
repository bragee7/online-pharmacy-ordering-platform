package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.DeliveryDto;
import com.medicare.pharmacy.entity.Delivery;
import com.medicare.pharmacy.entity.Order;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.DeliveryStatus;
import com.medicare.pharmacy.enums.OrderStatus;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.DeliveryRepository;
import com.medicare.pharmacy.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public DeliveryDto getByOrder(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));
        enforceAccess(delivery.getOrder());
        return toDto(delivery);
    }

    @Transactional
    public DeliveryDto updateStatus(Long orderId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));
        delivery.setStatus(status);
        if (status == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            Order order = delivery.getOrder();
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }
        Delivery saved = deliveryRepository.save(delivery);
        User current = safeCurrentUser();
        auditService.log(current != null ? current.getId() : null,
                "UPDATE_DELIVERY", "Delivery", saved.getId(),
                "Delivery for order " + orderId + " -> " + status);
        return toDto(saved);
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

    private DeliveryDto toDto(Delivery delivery) {
        return DeliveryDto.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrder().getId())
                .orderNumber(delivery.getOrder().getOrderNumber())
                .trackingNumber(delivery.getTrackingNumber())
                .carrier(delivery.getCarrier())
                .status(delivery.getStatus().name())
                .estimatedDeliveryDate(delivery.getEstimatedDeliveryDate())
                .deliveredAt(delivery.getDeliveredAt())
                .build();
    }
}
