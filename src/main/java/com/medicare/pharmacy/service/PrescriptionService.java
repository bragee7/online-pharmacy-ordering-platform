package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.PrescriptionDto;
import com.medicare.pharmacy.entity.Prescription;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.PrescriptionStatus;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.exception.BadRequestException;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.PrescriptionRepository;
import com.medicare.pharmacy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuthService authService;
    private final AuditService auditService;

    @Transactional
    public PrescriptionDto upload(MultipartFile file, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        String storedPath = fileStorageService.store(file, userId);
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : storedPath;
        Prescription pres = Prescription.builder()
                .user(user)
                .fileName(originalName)
                .filePath(storedPath)
                .fileType(file.getContentType())
                .status(PrescriptionStatus.PENDING)
                .build();
        Prescription saved = prescriptionRepository.save(pres);
        auditService.log(userId, "UPLOAD_PRESCRIPTION", "Prescription", saved.getId(),
                "Uploaded " + originalName);
        return PrescriptionDto.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionDto> listForUser(Long userId, Pageable pageable) {
        return prescriptionRepository.findByUserId(userId, pageable).map(PrescriptionDto::from);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionDto> listByStatus(PrescriptionStatus status, Pageable pageable) {
        return prescriptionRepository.findByStatus(status, pageable).map(PrescriptionDto::from);
    }

    @Transactional(readOnly = true)
    public PrescriptionDto get(Long id) {
        Prescription pres = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        enforceReadAccess(pres);
        return PrescriptionDto.from(pres);
    }

    @Transactional
    public PrescriptionDto review(Long id, boolean approve, String rejectionReason) {
        User current = authService.getCurrentUser();
        if (current.getRole() != Role.PHARMACIST && current.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only pharmacist or admin can review prescriptions");
        }
        Prescription pres = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        if (approve) {
            pres.setStatus(PrescriptionStatus.APPROVED);
            pres.setRejectionReason(null);
        } else {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw new BadRequestException("Rejection reason is required");
            }
            pres.setStatus(PrescriptionStatus.REJECTED);
            pres.setRejectionReason(rejectionReason);
        }
        pres.setReviewedAt(LocalDateTime.now());
        pres.setReviewedBy(current);
        Prescription saved = prescriptionRepository.save(pres);
        auditService.log(current.getId(),
                approve ? "APPROVE_PRESCRIPTION" : "REJECT_PRESCRIPTION",
                "Prescription", saved.getId(),
                "Reviewed prescription " + id + " approve=" + approve);
        return PrescriptionDto.from(saved);
    }

    @Transactional(readOnly = true)
    public Resource download(Long id) {
        Prescription pres = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        enforceReadAccess(pres);
        return fileStorageService.loadAsResource(pres.getFilePath());
    }

    private void enforceReadAccess(Prescription pres) {
        User current = authService.getCurrentUser();
        if (current.getRole() == Role.CUSTOMER
                && !pres.getUser().getId().equals(current.getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
