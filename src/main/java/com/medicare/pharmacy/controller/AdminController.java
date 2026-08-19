package com.medicare.pharmacy.controller;

import com.medicare.pharmacy.dto.ApiResponse;
import com.medicare.pharmacy.dto.UserDto;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.UserRepository;
import com.medicare.pharmacy.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("stats", dashboardService.stats());
        data.put("recentOrders", dashboardService.recentOrders(5));
        data.put("lowStock", dashboardService.lowStockMedicines());
        data.put("recentAudits", dashboardService.recentAudits(10));
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserDto>>> users(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserDto> page = userRepository.findAll(pageable).map(UserDto::from);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserDto>> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        Role role = Role.valueOf(body.get("role").toUpperCase());
        user.setRole(role);
        return ResponseEntity.ok(
                ApiResponse.ok(UserDto.from(userRepository.save(user)), "Role updated"));
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<ApiResponse<UserDto>> toggle(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setEnabled(!user.isEnabled());
        return ResponseEntity.ok(
                ApiResponse.ok(UserDto.from(userRepository.save(user)), "User status updated"));
    }
}
