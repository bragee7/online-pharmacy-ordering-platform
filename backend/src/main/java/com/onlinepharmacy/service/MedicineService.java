package com.onlinepharmacy.service;

import com.onlinepharmacy.dto.medicine.MedicineRequest;
import com.onlinepharmacy.dto.medicine.MedicineResponse;
import com.onlinepharmacy.entity.Category;
import com.onlinepharmacy.entity.Inventory;
import com.onlinepharmacy.entity.Medicine;
import com.onlinepharmacy.exception.DuplicateResourceException;
import com.onlinepharmacy.exception.ResourceNotFoundException;
import com.onlinepharmacy.mapper.MedicineMapper;
import com.onlinepharmacy.repository.CategoryRepository;
import com.onlinepharmacy.repository.InventoryRepository;
import com.onlinepharmacy.repository.MedicineRepository;
import com.onlinepharmacy.specification.MedicineSpecifications;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final MedicineMapper medicineMapper;

    public MedicineService(MedicineRepository medicineRepository,
                           CategoryRepository categoryRepository,
                           InventoryRepository inventoryRepository,
                           MedicineMapper medicineMapper) {
        this.medicineRepository = medicineRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.medicineMapper = medicineMapper;
    }

    @Transactional(readOnly = true)
    public Page<MedicineResponse> search(String q, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                         Boolean prescriptionRequired, Boolean inStock, Pageable pageable) {
        Page<Medicine> page = medicineRepository.findAll(
                MedicineSpecifications.withCriteria(q, categoryId, minPrice, maxPrice, prescriptionRequired, inStock),
                pageable);
        Map<Long, Inventory> stock = stocksFor(
                page.getContent().stream().map(Medicine::getId).toList());
        return page.map(medicine -> withStock(medicine, stock));
    }

    @Transactional(readOnly = true)
    public MedicineResponse getById(Long id) {
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Medicine", id));
        return withStock(medicine, inventories(medicine.getId()));
    }

    @Transactional
    public MedicineResponse create(MedicineRequest request) {
        if (medicineRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Medicine already exists: " + request.name());
        }
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Category", request.categoryId()));

        Medicine medicine = Medicine.builder()
                .name(request.name().trim())
                .genericName(request.genericName())
                .brandName(request.brandName())
                .description(request.description())
                .category(category)
                .manufacturer(request.manufacturer())
                .price(request.price())
                .prescriptionRequired(request.prescriptionRequired())
                .dosageInformation(request.dosageInformation())
                .expiryDate(request.expiryDate())
                .active(true)
                .build();
        medicineRepository.save(medicine);

        Inventory inventory = Inventory.builder()
                .medicine(medicine)
                .availableQuantity(0)
                .reservedQuantity(0)
                .reorderLevel(10)
                .build();
        inventoryRepository.save(inventory);

        return withStock(medicine, inventories(medicine.getId()));
    }

    @Transactional
    public MedicineResponse update(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Medicine", id));
        if (request.name() != null && !request.name().equalsIgnoreCase(medicine.getName())
                && medicineRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Medicine already exists: " + request.name());
        }

        Category category = request.categoryId() != null
                ? categoryRepository.findById(request.categoryId())
                        .orElseThrow(() -> ResourceNotFoundException.of("Category", request.categoryId()))
                : medicine.getCategory();

        applyRequest(medicine, request, category);
        return withStock(medicine, inventories(medicine.getId()));
    }

    @Transactional
    public void delete(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Medicine", id));
        // Soft delete: preserves order/cart/prescription history.
        medicine.setActive(false);
    }

    public Medicine getEntity(Long id) {
        return medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Medicine", id));
    }

    private void applyRequest(Medicine medicine, MedicineRequest request, Category category) {
        if (request.name() != null) {
            medicine.setName(request.name().trim());
        }
        medicine.setGenericName(request.genericName());
        medicine.setBrandName(request.brandName());
        medicine.setDescription(request.description());
        medicine.setCategory(category);
        medicine.setManufacturer(request.manufacturer());
        if (request.price() != null) {
            medicine.setPrice(request.price());
        }
        medicine.setPrescriptionRequired(request.prescriptionRequired());
        medicine.setDosageInformation(request.dosageInformation());
        medicine.setExpiryDate(request.expiryDate());
        if (request.active() != null) {
            medicine.setActive(request.active());
        }
    }

    private MedicineResponse withStock(Medicine medicine, Map<Long, Inventory> stock) {
        return medicineMapper.toResponse(medicine, stock);
    }

    private Map<Long, Inventory> inventories(Long medicineId) {
        return stocksFor(List.of(medicineId));
    }

    private Map<Long, Inventory> stocksFor(List<Long> medicineIds) {
        if (medicineIds.isEmpty()) {
            return Map.of();
        }
        return inventoryRepository.findByMedicineIdIn(medicineIds).stream()
                .collect(Collectors.toMap(inv -> inv.getMedicine().getId(), java.util.function.Function.identity()));
    }
}