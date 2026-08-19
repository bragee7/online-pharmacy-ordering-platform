package com.medicare.pharmacy.dto;

import com.medicare.pharmacy.entity.Prescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDto {
    private Long id;
    private Long userId;
    private String userName;
    private String fileName;
    private String fileType;
    private String status;
    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private String rejectionReason;

    public static PrescriptionDto from(Prescription prescription) {
        if (prescription == null) {
            return null;
        }
        return PrescriptionDto.builder()
                .id(prescription.getId())
                .userId(prescription.getUser() != null ? prescription.getUser().getId() : null)
                .userName(prescription.getUser() != null ? prescription.getUser().getName() : null)
                .fileName(prescription.getFileName())
                .fileType(prescription.getFileType())
                .status(prescription.getStatus() != null ? prescription.getStatus().name() : null)
                .uploadedAt(prescription.getUploadedAt())
                .reviewedAt(prescription.getReviewedAt())
                .reviewedBy(prescription.getReviewedBy() != null ? prescription.getReviewedBy().getId() : null)
                .rejectionReason(prescription.getRejectionReason())
                .build();
    }
}
