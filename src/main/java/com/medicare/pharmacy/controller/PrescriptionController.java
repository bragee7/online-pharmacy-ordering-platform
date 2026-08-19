package com.medicare.pharmacy.controller;

import com.medicare.pharmacy.dto.ApiResponse;
import com.medicare.pharmacy.dto.PrescriptionDto;
import com.medicare.pharmacy.enums.PrescriptionStatus;
import com.medicare.pharmacy.service.AuthService;
import com.medicare.pharmacy.service.PrescriptionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions")
@SecurityRequirement(name = "bearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final AuthService authService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PrescriptionDto>> upload(
            @RequestParam("file") MultipartFile file) {
        Long userId = authService.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(prescriptionService.upload(file, userId), "Prescription uploaded"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<PrescriptionDto>>> my(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.ok(prescriptionService.listForUser(authService.getCurrentUserId(), pageable)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<Page<PrescriptionDto>>> list(
            @RequestParam(required = false) PrescriptionStatus status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        PrescriptionStatus effective = status != null ? status : PrescriptionStatus.PENDING;
        return ResponseEntity.ok(ApiResponse.ok(prescriptionService.listByStatus(effective, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PrescriptionDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(prescriptionService.get(id)));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<PrescriptionDto>> review(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        boolean approve = body != null && Boolean.parseBoolean(String.valueOf(body.get("approve")));
        Object reasonObj = body != null
                ? (body.containsKey("rejectionReason") ? body.get("rejectionReason") : body.get("reason"))
                : null;
        String reason = reasonObj != null ? String.valueOf(reasonObj) : null;
        return ResponseEntity.ok(
                ApiResponse.ok(prescriptionService.review(id, approve, reason), "Prescription reviewed"));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        PrescriptionDto dto = prescriptionService.get(id);
        Resource resource = prescriptionService.download(id);
        String filename = dto.getFileName() != null ? dto.getFileName() : "prescription-" + id;
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
