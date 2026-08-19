package com.medicare.pharmacy.controller;

import com.medicare.pharmacy.dto.ApiResponse;
import com.medicare.pharmacy.dto.DeliveryDto;
import com.medicare.pharmacy.enums.DeliveryStatus;
import com.medicare.pharmacy.service.DeliveryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeliveryDto>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryService.getByOrder(orderId)));
    }

    @PutMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDto>> updateStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        DeliveryStatus status = DeliveryStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(
                ApiResponse.ok(deliveryService.updateStatus(orderId, status), "Delivery status updated"));
    }
}
