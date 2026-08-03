package com.onlinepharmacy.repository;

import com.onlinepharmacy.entity.Medicine;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MedicineRepository extends JpaRepository<Medicine, Long>, JpaSpecificationExecutor<Medicine> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Medicine> findByIdAndActiveTrue(Long id);
}