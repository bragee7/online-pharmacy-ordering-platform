package com.onlinepharmacy.repository;

import com.onlinepharmacy.entity.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    Optional<Address> findByUserIdAndDefaultAddressTrue(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}