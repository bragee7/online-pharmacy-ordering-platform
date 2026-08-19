package com.medicare.pharmacy.controller;

import com.medicare.pharmacy.dto.ApiResponse;
import com.medicare.pharmacy.dto.InventoryDto;
import com.medicare.pharmacy.service.InventoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> lowStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.lowStock()));
    }

    @GetMapping("/medicine/{medicineId}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<InventoryDto>> getByMedicine(@PathVariable Long medicineId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getByMedicine(medicineId)));
    }

    @PutMapping("/medicine/{medicineId}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<InventoryDto>> update(
            @PathVariable Long medicineId,
            @RequestBody Map<String, Integer> body) {
        Integer quantity = body != null ? body.get("quantity") : null;
        Integer reorderLevel = body != null ? body.get("reorderLevel") : null;
        return ResponseEntity.ok(
                ApiResponse.ok(inventoryService.update(medicineId, quantity, reorderLevel), "Inventory updated"));
    }
}
