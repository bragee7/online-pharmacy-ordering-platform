package com.onlinepharmacy.controller;

import com.onlinepharmacy.dto.medicine.MedicineRequest;
import com.onlinepharmacy.dto.medicine.MedicineResponse;
import com.onlinepharmacy.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medicines")
@Tag(name = "Medicines", description = "Catalogue — read for everyone (public), manage for ADMIN")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @Operation(summary = "Search/filter the medicines catalogue with pagination",
            description = "Filters: q (name/generic/brand), categoryId, minPrice, maxPrice, prescriptionRequired, inStock. Admin-only manage endpoints.")
    @GetMapping
    public Page<MedicineResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean prescriptionRequired,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {
        return medicineService.search(q, categoryId, minPrice, maxPrice, prescriptionRequired, inStock, pageable);
    }

    @Operation(summary = "Alias for the search endpoint")
    @GetMapping("/search")
    public Page<MedicineResponse> searchAlias(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean prescriptionRequired,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {
        return medicineService.search(q, categoryId, minPrice, maxPrice, prescriptionRequired, inStock, pageable);
    }

    @Operation(summary = "Get a medicine by id")
    @GetMapping("/{id}")
    public MedicineResponse getById(@PathVariable Long id) {
        return medicineService.getById(id);
    }

    @Operation(summary = "Create a medicine (admin)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.create(request));
    }

    @Operation(summary = "Update a medicine (admin)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MedicineResponse update(@PathVariable Long id, @Valid @RequestBody MedicineRequest request) {
        return medicineService.update(id, request);
    }

    @Operation(summary = "Delete (deactivate) a medicine (admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}