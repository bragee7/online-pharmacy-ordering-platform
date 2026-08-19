package com.medicare.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDto {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private String trackingNumber;
    private String carrier;
    private String status;
    private LocalDate estimatedDeliveryDate;
    private LocalDateTime deliveredAt;
}
