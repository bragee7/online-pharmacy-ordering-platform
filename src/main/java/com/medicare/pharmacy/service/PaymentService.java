package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.PaymentDto;
import com.medicare.pharmacy.entity.Order;
import com.medicare.pharmacy.entity.Payment;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.PaymentStatus;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.OrderRepository;
import com.medicare.pharmacy.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PaymentDto getByOrder(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        enforceAccess(payment.getOrder());
        return toDto(payment);
    }

    @Transactional
    public PaymentDto markPaid(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        User current = safeCurrentUser();
        auditService.log(current != null ? current.getId() : null,
                "PAYMENT_SUCCESS", "Payment", saved.getId(), "Marked paid for order " + orderId);
        return toDto(saved);
    }

    @Transactional
    public PaymentDto refund(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        User current = safeCurrentUser();
        auditService.log(current != null ? current.getId() : null,
                "PAYMENT_REFUND", "Payment", saved.getId(), "Refunded payment for order " + orderId);
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

    private PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus().name())
                .transactionReference(payment.getTransactionReference())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
