package com.medicare.pharmacy.repository;

import com.medicare.pharmacy.entity.Prescription;
import com.medicare.pharmacy.enums.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Page<Prescription> findByUserId(Long userId, Pageable pageable);
    Page<Prescription> findByStatus(PrescriptionStatus status, Pageable pageable);
    List<Prescription> findByUserIdAndStatus(Long userId, PrescriptionStatus status);
    long countByStatus(PrescriptionStatus status);
}
