package com.onlinepharmacy.repository;

import com.onlinepharmacy.entity.ERole;
import com.onlinepharmacy.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u join fetch u.roles where u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    boolean existsByEmail(String email);

    List<User> findByActiveTrueAndRolesNameOrderByCreatedAtDesc(ERole role);

    @Query("select count(u) from User u join u.roles r where r.name = :role and u.active = true")
    long countActiveUsersByRole(@Param("role") ERole role);
}