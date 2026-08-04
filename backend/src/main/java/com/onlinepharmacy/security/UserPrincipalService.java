package com.onlinepharmacy.security;

import com.onlinepharmacy.entity.ERole;
import com.onlinepharmacy.entity.User;
import com.onlinepharmacy.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads users by email (for login) and by id (for JWT requests).
 */
@Service
@Transactional(readOnly = true)
public class UserPrincipalService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserPrincipalService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return UserPrincipal.from(loadUserByEmail(email));
    }

    public UserPrincipal loadById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return UserPrincipal.from(user);
    }

    public User loadUserByEmail(String email) {
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public User loadEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }

    public boolean isRole(Long userId, ERole role) {
        return userRepository.findById(userId)
                .map(u -> u.hasRole(role))
                .orElse(false);
    }
}