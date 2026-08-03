package com.onlinepharmacy.repository;

import com.onlinepharmacy.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"items", "items.medicine", "items.medicine.inventory"})
    Optional<Cart> findByUserId(Long userId);
}