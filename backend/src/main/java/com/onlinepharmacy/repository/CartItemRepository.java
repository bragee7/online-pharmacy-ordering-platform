package com.onlinepharmacy.repository;

import com.onlinepharmacy.entity.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndMedicineId(Long cartId, Long medicineId);

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    void deleteByCartId(Long cartId);
}