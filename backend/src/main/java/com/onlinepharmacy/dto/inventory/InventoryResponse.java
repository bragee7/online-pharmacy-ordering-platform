package com.onlinepharmacy.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Inventory snapshot for a medicine")
public record InventoryResponse(
        Long id,
        Long medicineId,
        String medicineName,
        int availableQuantity,
        int reservedQuantity,
        int reorderLevel,
        @Schema(description = "available - reserved")
        int effectiveAvailable,
        boolean lowStock,
        Instant lastUpdated
) {}