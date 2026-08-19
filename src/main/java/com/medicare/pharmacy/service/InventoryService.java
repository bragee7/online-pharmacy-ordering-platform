package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.InventoryDto;
import com.medicare.pharmacy.entity.Inventory;
import com.medicare.pharmacy.entity.Medicine;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.InventoryRepository;
import com.medicare.pharmacy.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;

    @Transactional(readOnly = true)
    public InventoryDto getByMedicine(Long medicineId) {
        Inventory inv = inventoryRepository.findByMedicineId(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "medicineId", medicineId));
        return InventoryDto.from(inv);
    }

    @Transactional
    public InventoryDto update(Long medicineId, Integer quantity, Integer reorderLevel) {
        Inventory inv = inventoryRepository.findByMedicineId(medicineId).orElse(null);
        if (inv == null) {
            Medicine med = medicineRepository.findById(medicineId)
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));
            inv = Inventory.builder()
                    .medicine(med)
                    .quantity(quantity != null ? quantity : 0)
                    .reservedQuantity(0)
                    .reorderLevel(reorderLevel != null ? reorderLevel : 10)
                    .build();
        } else {
            if (quantity != null) {
                inv.setQuantity(quantity);
            }
            if (reorderLevel != null) {
                inv.setReorderLevel(reorderLevel);
            }
        }
        Inventory saved = inventoryRepository.save(inv);
        return InventoryDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryDto> lowStock() {
        return inventoryRepository.findLowStock().stream()
                .map(InventoryDto::from)
                .toList();
    }
}
