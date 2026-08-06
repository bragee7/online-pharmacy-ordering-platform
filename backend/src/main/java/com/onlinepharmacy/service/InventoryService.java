package com.onlinepharmacy.service;

import com.onlinepharmacy.dto.inventory.InventoryResponse;
import com.onlinepharmacy.dto.inventory.InventoryUpdateRequest;
import com.onlinepharmacy.entity.Inventory;
import com.onlinepharmacy.entity.Medicine;
import com.onlinepharmacy.exception.InsufficientStockException;
import com.onlinepharmacy.exception.ResourceNotFoundException;
import com.onlinepharmacy.repository.InventoryRepository;
import com.onlinepharmacy.repository.MedicineRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;

    public InventoryService(InventoryRepository inventoryRepository, MedicineRepository medicineRepository) {
        this.inventoryRepository = inventoryRepository;
        this.medicineRepository = medicineRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> listAll() {
        return inventoryRepository.findAll().stream()
                .map(InventoryService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> lowStock() {
        return inventoryRepository.findLowStock().stream()
                .map(InventoryService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse getForMedicine(Long medicineId) {
        return toResponse(getEntity(medicineId));
    }

    @Transactional
    public InventoryResponse restock(Long medicineId, InventoryUpdateRequest request) {
        Inventory inventory = getEntity(medicineId);
        inventory.setAvailableQuantity(request.availableQuantity());
        inventory.setReorderLevel(request.reorderLevel());
        return toResponse(inventory);
    }

    /**
     * Reserves stock inside the caller's transaction (MANDATORY). Uses a
     * pessimistic write lock so concurrent orders cannot oversell the last unit.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void reserve(Long medicineId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Inventory inventory = getLocked(medicineId);
        if (inventory.effectiveAvailable() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for '%s': requested %d, available %d"
                            .formatted(inventory.getMedicine().getName(), quantity, inventory.effectiveAvailable()));
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
    }

    /** Releases a reservation back to available stock (within the caller's transaction). */
    @Transactional(propagation = Propagation.MANDATORY)
    public void release(Long medicineId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Inventory inventory = getLocked(medicineId);
        int newReserved = inventory.getReservedQuantity() - quantity;
        if (newReserved < 0) {
            newReserved = 0;
        }
        inventory.setReservedQuantity(newReserved);
    }

    /**
     * Completes a fulfilled order: removes quantity from both available and reserved.
     * Guards against negative available quantity.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void deduct(Long medicineId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Inventory inventory = getLocked(medicineId);
        int newReserved = inventory.getReservedQuantity() - quantity;
        int newAvailable = inventory.getAvailableQuantity() - quantity;
        if (newAvailable < 0) {
            throw new InsufficientStockException(
                    "Cannot deduct stock below zero for '%s'".formatted(inventory.getMedicine().getName()));
        }
        inventory.setAvailableQuantity(newAvailable);
        inventory.setReservedQuantity(Math.max(newReserved, 0));
    }

    private Inventory getEntity(Long medicineId) {
        return inventoryRepository.findByMedicineId(medicineId)
                .orElseThrow(() -> ResourceNotFoundException.of("Inventory for medicine", medicineId));
    }

    private Inventory getLocked(Long medicineId) {
        return inventoryRepository.findByMedicineIdForUpdate(medicineId)
                .orElseThrow(() -> ResourceNotFoundException.of("Inventory for medicine", medicineId));
    }

    public static InventoryResponse toResponse(Inventory inventory) {
        Medicine medicine = inventory.getMedicine();
        return new InventoryResponse(
                inventory.getId(),
                medicine != null ? medicine.getId() : null,
                medicine != null ? medicine.getName() : "?",
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getReorderLevel(),
                inventory.effectiveAvailable(),
                inventory.isLowStock(),
                inventory.getLastUpdated());
    }
}