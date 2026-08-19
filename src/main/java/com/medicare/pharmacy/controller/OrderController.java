package com.medicare.pharmacy.controller;

import com.medicare.pharmacy.dto.ApiResponse;
import com.medicare.pharmacy.dto.CheckoutRequest;
import com.medicare.pharmacy.dto.OrderDto;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.OrderStatus;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.service.AuthService;
import com.medicare.pharmacy.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderDto>> checkout(@Valid @RequestBody CheckoutRequest request) {
        Long userId = authService.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.checkout(userId, request), "Order placed"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> my(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.ok(orderService.listForUser(authService.getCurrentUserId(), pageable)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> list(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.listAll(status, pageable)));
    }

    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDto>> getByNumber(@PathVariable String orderNumber) {
        OrderDto dto = orderService.getByOrderNumber(orderNumber);
        enforceOwnerAccess(dto);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDto>> get(@PathVariable Long id) {
        OrderDto dto = orderService.getById(id);
        enforceOwnerAccess(dto);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(
                ApiResponse.ok(orderService.updateStatus(id, status), "Order status updated"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> cancel(@PathVariable Long id) {
        Long currentId = authService.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(orderService.cancel(id, currentId), "Order cancelled"));
    }

    private void enforceOwnerAccess(OrderDto dto) {
        User current = authService.getCurrentUser();
        if (current.getRole() == Role.CUSTOMER && !dto.getUserId().equals(current.getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
