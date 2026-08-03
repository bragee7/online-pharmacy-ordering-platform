package com.onlinepharmacy.repository;

import com.onlinepharmacy.entity.Prescription;
import com.onlinepharmacy.entity.PrescriptionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @EntityGraph(attributePaths = {"items", "items.medicine"})
    List<Prescription> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "items.medicine"})
    List<Prescription> findByStatusOrderByCreatedAtAsc(PrescriptionStatus status);

    @EntityGraph(attributePaths = {"items", "items.medicine"})
    Optional<Prescription> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"items", "items.medicine"})
    Optional<Prescription> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"items", "items.medicine"})
    List<Prescription> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, PrescriptionStatus status);

    long countByStatus(PrescriptionStatus status);
}