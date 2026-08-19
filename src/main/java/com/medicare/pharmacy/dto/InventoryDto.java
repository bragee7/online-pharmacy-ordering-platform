package com.medicare.pharmacy.dto;

import com.medicare.pharmacy.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDto {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private int quantity;
    private int reservedQuantity;
    private int available;
    private int reorderLevel;
    private boolean lowStock;

    public static InventoryDto from(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        return InventoryDto.builder()
                .id(inventory.getId())
                .medicineId(inventory.getMedicine() != null ? inventory.getMedicine().getId() : null)
                .medicineName(inventory.getMedicine() != null ? inventory.getMedicine().getName() : null)
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .available(inventory.getAvailable())
                .reorderLevel(inventory.getReorderLevel())
                .lowStock(inventory.isLowStock())
                .build();
    }
}
