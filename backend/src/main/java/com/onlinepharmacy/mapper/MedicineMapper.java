package com.onlinepharmacy.mapper;

import com.onlinepharmacy.dto.medicine.MedicineResponse;
import com.onlinepharmacy.entity.Inventory;
import com.onlinepharmacy.entity.Medicine;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MedicineMapper {

    public MedicineResponse toResponse(Medicine medicine, Map<Long, Inventory> inventoryByMedicineId) {
        Inventory inventory = inventoryByMedicineId.get(medicine.getId());
        int available = inventory == null ? 0 : inventory.effectiveAvailable();
        boolean inStock = available > 0;
        return new MedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getGenericName(),
                medicine.getBrandName(),
                medicine.getDescription(),
                medicine.getCategory() != null ? medicine.getCategory().getId() : null,
                medicine.getCategory() != null ? medicine.getCategory().getName() : null,
                medicine.getManufacturer(),
                medicine.getPrice(),
                medicine.isPrescriptionRequired(),
                medicine.getDosageInformation(),
                medicine.getExpiryDate(),
                medicine.isActive(),
                available,
                inStock);
    }
}