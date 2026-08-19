package com.medicare.pharmacy;

import com.medicare.pharmacy.entity.Category;
import com.medicare.pharmacy.entity.Inventory;
import com.medicare.pharmacy.entity.Medicine;
import com.medicare.pharmacy.repository.CategoryRepository;
import com.medicare.pharmacy.repository.InventoryRepository;
import com.medicare.pharmacy.repository.MedicineRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InventoryLowStockTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void findLowStock_returnsLowItems() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Category cat = categoryRepository.save(Category.builder()
                .name("TestCat-" + suffix)
                .description("test category")
                .build());

        Medicine lowMed = medicineRepository.save(Medicine.builder()
                .name("LowMed-" + suffix)
                .price(new BigDecimal("10.00"))
                .prescriptionRequired(false)
                .category(cat)
                .active(true)
                .build());
        inventoryRepository.save(Inventory.builder()
                .medicine(lowMed)
                .quantity(2)
                .reservedQuantity(0)
                .reorderLevel(10)
                .build());

        Medicine okMed = medicineRepository.save(Medicine.builder()
                .name("OkMed-" + suffix)
                .price(new BigDecimal("20.00"))
                .prescriptionRequired(false)
                .category(cat)
                .active(true)
                .build());
        inventoryRepository.save(Inventory.builder()
                .medicine(okMed)
                .quantity(50)
                .reservedQuantity(0)
                .reorderLevel(10)
                .build());

        List<Inventory> low = inventoryRepository.findLowStock();

        assertTrue(low.stream().anyMatch(i -> i.getMedicine().getId().equals(lowMed.getId())),
                "low-stock medicine should be returned");
        assertTrue(low.stream().noneMatch(i -> i.getMedicine().getId().equals(okMed.getId())),
                "well-stocked medicine should not be returned");
    }
}
