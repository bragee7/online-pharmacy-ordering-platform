package com.onlinepharmacy.repository;

import com.onlinepharmacy.entity.Order;
import com.onlinepharmacy.entity.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"items", "items.medicine", "payment", "statusHistory"})
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

    @EntityGraph(attributePaths = {"items", "payment"})
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"customer", "items", "payment"})
    Optional<Order> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"customer", "items", "payment"})
    List<Order> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"customer", "items", "payment"})
    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    long countByStatus(OrderStatus status);
}