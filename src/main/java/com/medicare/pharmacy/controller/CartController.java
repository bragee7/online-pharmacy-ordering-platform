package com.medicare.pharmacy.controller;

import com.medicare.pharmacy.dto.ApiResponse;
import com.medicare.pharmacy.dto.CartDto;
import com.medicare.pharmacy.service.AuthService;
import com.medicare.pharmacy.service.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> getCart() {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(authService.getCurrentUserId())));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> addItem(@RequestBody Map<String, Object> body) {
        Long userId = authService.getCurrentUserId();
        Long medicineId = ((Number) body.get("medicineId")).longValue();
        int quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;
        return ResponseEntity.ok(
                ApiResponse.ok(cartService.addItem(userId, medicineId, quantity), "Item added"));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> updateItem(
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> body) {
        Long userId = authService.getCurrentUserId();
        int quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;
        return ResponseEntity.ok(
                ApiResponse.ok(cartService.updateItem(userId, itemId, quantity), "Item updated"));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(@PathVariable Long itemId) {
        Long userId = authService.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(cartService.removeItem(userId, itemId), "Item removed"));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> clear() {
        cartService.clear(authService.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Cart cleared"));
    }
}
