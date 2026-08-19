package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.MedicineDto;
import com.medicare.pharmacy.dto.MedicineRequest;
import com.medicare.pharmacy.entity.Category;
import com.medicare.pharmacy.entity.Inventory;
import com.medicare.pharmacy.entity.Medicine;
import com.medicare.pharmacy.exception.BadRequestException;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.CategoryRepository;
import com.medicare.pharmacy.repository.InventoryRepository;
import com.medicare.pharmacy.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<MedicineDto> search(String keyword, Long categoryId, Boolean rx, int page, int size, String sort) {
        String kw = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();
        Pageable pageable = buildPageable(page, size, sort);
        Page<Medicine> result = medicineRepository.search(kw, categoryId, rx, pageable);
        return result.map(med -> {
            int available = inventoryRepository.findByMedicineId(med.getId())
                    .map(Inventory::getAvailable)
                    .orElse(0);
            return MedicineDto.from(med, available);
        });
    }

    @Transactional(readOnly = true)
    public MedicineDto getById(Long id) {
        Medicine med = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", id));
        int available = inventoryRepository.findByMedicineId(med.getId())
                .map(Inventory::getAvailable)
                .orElse(0);
        return MedicineDto.from(med, available);
    }

    @Transactional
    public MedicineDto create(MedicineRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BadRequestException("Medicine name is required");
        }
        if (req.getPrice() == null) {
            throw new BadRequestException("Medicine price is required");
        }
        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getCategoryId()));
        }
        Medicine med = Medicine.builder()
                .name(req.getName().trim())
                .genericName(req.getGenericName())
                .brandName(req.getBrandName())
                .description(req.getDescription())
                .manufacturer(req.getManufacturer())
                .price(req.getPrice())
                .prescriptionRequired(req.isPrescriptionRequired())
                .category(category)
                .imageUrl(req.getImageUrl())
                .active(true)
                .build();
        Medicine saved = medicineRepository.save(med);
        int qty = req.getInitialStock();
        int reorder = req.getReorderLevel();
        Inventory inv = Inventory.builder()
                .medicine(saved)
                .quantity(qty)
                .reservedQuantity(0)
                .reorderLevel(reorder)
                .build();
        inventoryRepository.save(inv);
        auditService.log(null, "CREATE_MEDICINE", "Medicine", saved.getId(),
                "Created medicine: " + saved.getName());
        return MedicineDto.from(saved, inv.getAvailable());
    }

    @Transactional
    public MedicineDto update(Long id, MedicineRequest req) {
        Medicine med = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", id));
        if (req.getName() != null && !req.getName().isBlank()) {
            med.setName(req.getName().trim());
        }
        if (req.getGenericName() != null) {
            med.setGenericName(req.getGenericName());
        }
        if (req.getBrandName() != null) {
            med.setBrandName(req.getBrandName());
        }
        if (req.getDescription() != null) {
            med.setDescription(req.getDescription());
        }
        if (req.getManufacturer() != null) {
            med.setManufacturer(req.getManufacturer());
        }
        if (req.getPrice() != null) {
            med.setPrice(req.getPrice());
        }
        med.setPrescriptionRequired(req.isPrescriptionRequired());
        if (req.getImageUrl() != null) {
            med.setImageUrl(req.getImageUrl());
        }
        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", req.getCategoryId()));
            med.setCategory(category);
        }
        Medicine saved = medicineRepository.save(med);
        int available = inventoryRepository.findByMedicineId(saved.getId())
                .map(Inventory::getAvailable)
                .orElse(0);
        auditService.log(null, "UPDATE_MEDICINE", "Medicine", saved.getId(),
                "Updated medicine: " + saved.getName());
        return MedicineDto.from(saved, available);
    }

    @Transactional
    public MedicineDto softDelete(Long id) {
        Medicine med = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", id));
        med.setActive(false);
        Medicine saved = medicineRepository.save(med);
        int available = inventoryRepository.findByMedicineId(saved.getId())
                .map(Inventory::getAvailable)
                .orElse(0);
        auditService.log(null, "DELETE_MEDICINE", "Medicine", saved.getId(),
                "Soft-deleted medicine: " + saved.getName());
        return MedicineDto.from(saved, available);
    }

    @Transactional
    public MedicineDto restore(Long id) {
        Medicine med = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", id));
        med.setActive(true);
        Medicine saved = medicineRepository.save(med);
        int available = inventoryRepository.findByMedicineId(saved.getId())
                .map(Inventory::getAvailable)
                .orElse(0);
        auditService.log(null, "RESTORE_MEDICINE", "Medicine", saved.getId(),
                "Restored medicine: " + saved.getName());
        return MedicineDto.from(saved, available);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        int p = Math.max(page, 0);
        int s = size <= 0 ? 20 : size;
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
        }
        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(p, s, Sort.by(dir, prop));
    }
}
