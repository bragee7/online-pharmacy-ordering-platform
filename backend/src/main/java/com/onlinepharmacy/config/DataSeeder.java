package com.onlinepharmacy.config;

import com.onlinepharmacy.entity.Cart;
import com.onlinepharmacy.entity.ERole;
import com.onlinepharmacy.entity.Role;
import com.onlinepharmacy.entity.User;
import com.onlinepharmacy.repository.CartRepository;
import com.onlinepharmacy.repository.RoleRepository;
import com.onlinepharmacy.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds development users (admin, pharmacist, customers) on first boot.
 * Passwords are BCrypt-hashed. Only runs when {@code app.seed.enabled=true}.
 */
@Configuration
@Slf4j
public class DataSeeder {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository,
                                       RoleRepository roleRepository,
                                       CartRepository cartRepository,
                                       PasswordEncoder passwordEncoder,
                                       @Value("${app.seed.enabled}") boolean enabled) {
        return args -> {
            if (!enabled) {
                log.info("Data seeding disabled (app.seed.enabled=false)");
                return;
            }
            if (userRepository.count() > 0) {
                log.info("Data seeding skipped: users already present");
                return;
            }

            Role customerRole = roleRepository.findByName(ERole.CUSTOMER).orElseThrow();
            Role pharmacistRole = roleRepository.findByName(ERole.PHARMACIST).orElseThrow();
            Role adminRole = roleRepository.findByName(ERole.ADMIN).orElseThrow();

            createUser(userRepository, cartRepository, passwordEncoder,
                    "Admin", "admin@example.com", "Admin@123", "9800000000", adminRole);
            createUser(userRepository, cartRepository, passwordEncoder,
                    "Pharmacist", "pharmacist@example.com", "Pharmacist@123", "9800000001", pharmacistRole);
            createUser(userRepository, cartRepository, passwordEncoder,
                    "Customer One", "customer@example.com", "Customer@123", "9800000002", customerRole);
            createUser(userRepository, cartRepository, passwordEncoder,
                    "Customer Two", "customer2@example.com", "Customer@123", "9800000003", customerRole);

            log.info("Seeded 4 demo accounts (admin, pharmacist, 2 customers) — see README for credentials");
        };
    }

    private void createUser(UserRepository userRepository, CartRepository cartRepository,
                            PasswordEncoder encoder, String name, String email,
                            String rawPassword, String phone, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encoder.encode(rawPassword));
        user.setPhone(phone);
        user.addRole(role);
        userRepository.save(user);
        cartRepository.save(new Cart(user));
    }
}