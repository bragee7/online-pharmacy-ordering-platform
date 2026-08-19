package com.medicare.pharmacy;

import com.medicare.pharmacy.dto.MedicineDto;
import com.medicare.pharmacy.dto.MedicineRequest;
import com.medicare.pharmacy.entity.Inventory;
import com.medicare.pharmacy.entity.Medicine;
import com.medicare.pharmacy.repository.CategoryRepository;
import com.medicare.pharmacy.repository.InventoryRepository;
import com.medicare.pharmacy.repository.MedicineRepository;
import com.medicare.pharmacy.service.AuditService;
import com.medicare.pharmacy.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private MedicineService medicineService;

    @Test
    void create_success() {
        MedicineRequest req = MedicineRequest.builder()
                .name("Paracetamol 500mg")
                .genericName("Paracetamol")
                .price(new BigDecimal("19.99"))
                .prescriptionRequired(false)
                .initialStock(100)
                .reorderLevel(10)
                .build();

        Medicine saved = Medicine.builder()
                .id(1L)
                .name("Paracetamol 500mg")
                .genericName("Paracetamol")
                .price(new BigDecimal("19.99"))
                .prescriptionRequired(false)
                .active(true)
                .build();
        when(medicineRepository.save(any(Medicine.class))).thenReturn(saved);

        Inventory inv = Inventory.builder()
                .id(1L)
                .medicine(saved)
                .quantity(100)
                .reservedQuantity(0)
                .reorderLevel(10)
                .build();
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inv);

        MedicineDto dto = medicineService.create(req);

        assertNotNull(dto);
        assertEquals("Paracetamol 500mg", dto.getName());
        assertEquals(100, dto.getStock());
        assertTrue(dto.isActive());
    }

    @Test
    void search_returnsPage() {
        Medicine med = Medicine.builder()
                .id(1L)
                .name("Ibuprofen 200mg")
                .price(new BigDecimal("29.99"))
                .prescriptionRequired(false)
                .active(true)
                .build();
        Page<Medicine> page = new PageImpl<>(List.of(med));
        when(medicineRepository.search(any(), any(), any(), any(Pageable.class))).thenReturn(page);

        Inventory inv = Inventory.builder()
                .id(1L)
                .medicine(med)
                .quantity(50)
                .reservedQuantity(5)
                .reorderLevel(10)
                .build();
        when(inventoryRepository.findByMedicineId(1L)).thenReturn(Optional.of(inv));

        Page<MedicineDto> result = medicineService.search("ibu", null, null, 0, 20, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("Ibuprofen 200mg", result.getContent().get(0).getName());
        assertEquals(45, result.getContent().get(0).getStock());
    }
}
