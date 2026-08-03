package com.onlinepharmacy.repository;

import com.onlinepharmacy.entity.ERole;
import com.onlinepharmacy.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);

    boolean existsByName(ERole name);
}