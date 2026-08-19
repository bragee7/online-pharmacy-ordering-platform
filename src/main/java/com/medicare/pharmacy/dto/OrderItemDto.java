package com.medicare.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
