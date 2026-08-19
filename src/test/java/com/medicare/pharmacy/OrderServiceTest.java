package com.medicare.pharmacy;

import com.medicare.pharmacy.dto.CheckoutRequest;
import com.medicare.pharmacy.dto.OrderDto;
import com.medicare.pharmacy.entity.Cart;
import com.medicare.pharmacy.entity.CartItem;
import com.medicare.pharmacy.entity.Inventory;
import com.medicare.pharmacy.entity.Medicine;
import com.medicare.pharmacy.entity.Order;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.exception.BadRequestException;
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
import com.medicare.pharmacy.service.AuditService;
import com.medicare.pharmacy.service.AuthService;
import com.medicare.pharmacy.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private MedicineRepository medicineRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthService authService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void checkout_success() {
        Long userId = 1L;
        User user = User.builder().id(userId).name("Customer").email("customer@medicare.com")
                .password("x").role(Role.CUSTOMER).enabled(true).build();
        Cart cart = Cart.builder().id(10L).user(user).build();

        Medicine med = Medicine.builder().id(5L).name("Vitamin C")
                .price(new BigDecimal("19.99")).prescriptionRequired(false).active(true).build();
        CartItem ci = CartItem.builder().id(100L).cart(cart).medicine(med).quantity(2).build();

        Inventory inv = Inventory.builder().id(50L).medicine(med)
                .quantity(10).reservedQuantity(0).reorderLevel(2).build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(ci));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(prescriptionRepository.findByUserIdAndStatus(any(), any())).thenReturn(Collections.emptyList());
        when(inventoryRepository.findByMedicineId(5L)).thenReturn(Optional.of(inv));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1L);
            return o;
        });
        when(orderItemRepository.findByOrderId(any())).thenReturn(Collections.emptyList());
        when(paymentRepository.findByOrderId(any())).thenReturn(Optional.empty());
        when(deliveryRepository.findByOrderId(any())).thenReturn(Optional.empty());

        CheckoutRequest req = CheckoutRequest.builder()
                .shippingAddress("123 Main St, Berlin")
                .paymentMethod("COD")
                .build();

        OrderDto dto = orderService.checkout(userId, req);

        assertNotNull(dto);
        assertEquals(new BigDecimal("39.98"), dto.getTotalAmount());
        verify(orderRepository).save(any(Order.class));
        // inventory decremented: 10 - 2 = 8
        assertEquals(8, inv.getQuantity());
        verify(cartItemRepository).deleteByCartId(10L);
    }

    @Test
    void checkout_emptyCart_throwsBadRequest() {
        Long userId = 2L;
        User user = User.builder().id(userId).name("C2").email("c2@medicare.com")
                .password("x").role(Role.CUSTOMER).enabled(true).build();
        Cart cart = Cart.builder().id(20L).user(user).build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(20L)).thenReturn(Collections.emptyList());

        CheckoutRequest req = CheckoutRequest.builder()
                .shippingAddress("123 Main St")
                .paymentMethod("COD")
                .build();

        assertThrows(BadRequestException.class, () -> orderService.checkout(userId, req));
    }
}
