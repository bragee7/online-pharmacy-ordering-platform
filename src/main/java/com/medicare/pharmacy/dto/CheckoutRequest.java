package com.medicare.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {
    @NotBlank
    private String shippingAddress;

    @Builder.Default
    private String paymentMethod = "COD";

    private List<Long> prescriptionIds;
}
