package com.medicare.pharmacy.repository;

import com.medicare.pharmacy.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    @Query("SELECT m FROM Medicine m WHERE (:keyword = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.brandName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:categoryId IS NULL OR m.category.id = :categoryId) AND (:rx IS NULL OR m.prescriptionRequired = :rx)")
    Page<Medicine> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("rx") Boolean rx, Pageable pageable);

    Page<Medicine> findByActiveTrue(Pageable pageable);
    long countByActiveTrue();
}
