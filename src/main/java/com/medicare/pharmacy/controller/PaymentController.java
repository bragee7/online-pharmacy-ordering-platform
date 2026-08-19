package com.medicare.pharmacy.controller;

import com.medicare.pharmacy.dto.ApiResponse;
import com.medicare.pharmacy.dto.PaymentDto;
import com.medicare.pharmacy.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentDto>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getByOrder(orderId)));
    }

    @PostMapping("/order/{orderId}/pay")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> pay(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.markPaid(orderId), "Payment marked as paid"));
    }

    @PostMapping("/order/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> refund(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.refund(orderId), "Payment refunded"));
    }
}
