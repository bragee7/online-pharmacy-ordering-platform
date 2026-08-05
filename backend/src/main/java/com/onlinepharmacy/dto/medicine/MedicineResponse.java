package com.onlinepharmacy.dto.medicine;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Medicine as returned by the catalogue endpoints")
public record MedicineResponse(
        Long id,
        String name,
        String genericName,
        String brandName,
        String description,
        Long categoryId,
        String categoryName,
        String manufacturer,
        BigDecimal price,
        boolean prescriptionRequired,
        String dosageInformation,
        LocalDate expiryDate,
        boolean active,
        @Schema(description = "Effective available quantity (available - reserved)")
        int availableQuantity,
        boolean inStock
) {}