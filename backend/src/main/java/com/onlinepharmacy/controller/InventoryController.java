package com.onlinepharmacy.controller;

import com.onlinepharmacy.dto.inventory.InventoryResponse;
import com.onlinepharmacy.dto.inventory.InventoryUpdateRequest;
import com.onlinepharmacy.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Stock visibility for PHARMACIST/ADMIN, restock for ADMIN")
@PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "List all inventory entries")
    @GetMapping
    public List<InventoryResponse> listAll() {
        return inventoryService.listAll();
    }

    @Operation(summary = "List low-stock medicines")
    @GetMapping("/low-stock")
    public List<InventoryResponse> lowStock() {
        return inventoryService.lowStock();
    }

    @Operation(summary = "Get inventory for one medicine")
    @GetMapping("/medicines/{medicineId}")
    public InventoryResponse getForMedicine(@PathVariable Long medicineId) {
        return inventoryService.getForMedicine(medicineId);
    }

    @Operation(summary = "Restock a medicine (admin)")
    @PutMapping("/medicines/{medicineId}")
    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse restock(@PathVariable Long medicineId,
                                     @Valid @RequestBody InventoryUpdateRequest request) {
        return inventoryService.restock(medicineId, request);
    }
}