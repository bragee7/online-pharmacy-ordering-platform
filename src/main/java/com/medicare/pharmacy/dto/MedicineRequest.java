package com.medicare.pharmacy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineRequest {
    @NotBlank
    private String name;

    private String genericName;

    private String brandName;

    private String description;

    private String manufacturer;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @Builder.Default
    private boolean prescriptionRequired = false;

    private Long categoryId;

    private String imageUrl;

    @Builder.Default
    @Min(0)
    private int initialStock = 0;

    @Builder.Default
    @Min(0)
    private int reorderLevel = 10;
}
