package com.onlinepharmacy.repository;

import com.onlinepharmacy.entity.Inventory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByMedicineId(Long medicineId);

    List<Inventory> findByMedicineIdIn(java.util.Collection<Long> medicineIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.medicine.id = :medicineId")
    Optional<Inventory> findByMedicineIdForUpdate(@Param("medicineId") Long medicineId);

    @Query("""
            select i from Inventory i
            join fetch i.medicine m
            where m.active = true
              and (i.availableQuantity - i.reservedQuantity) <= i.reorderLevel
            order by (i.availableQuantity - i.reservedQuantity) asc
            """)
    List<Inventory> findLowStock();
}