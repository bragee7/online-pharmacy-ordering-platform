package com.onlinepharmacy.dto.medicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Body for creating or updating a medicine (admin only)")
public record MedicineRequest(
        @NotBlank(message = "Medicine name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Size(max = 150, message = "Generic name must be at most 150 characters")
        String genericName,

        @Size(max = 150, message = "Brand name must be at most 150 characters")
        String brandName,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotNull(message = "Category id is required")
        @Positive(message = "Category id must be positive")
        Long categoryId,

        @Size(max = 150, message = "Manufacturer must be at most 150 characters")
        String manufacturer,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
        BigDecimal price,

        @Schema(description = "Whether a valid approved prescription is mandatory to order")
        boolean prescriptionRequired,

        @Size(max = 255, message = "Dosage information must be at most 255 characters")
        String dosageInformation,

        LocalDate expiryDate,

        @Schema(description = "Only meaningful on update; ignored when creating")
        Boolean active
) {}