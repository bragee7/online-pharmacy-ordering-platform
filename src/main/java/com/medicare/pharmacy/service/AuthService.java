package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.AuthResponse;
import com.medicare.pharmacy.dto.LoginRequest;
import com.medicare.pharmacy.dto.RegisterRequest;
import com.medicare.pharmacy.dto.UserDto;
import com.medicare.pharmacy.entity.Cart;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.exception.BadRequestException;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.CartRepository;
import com.medicare.pharmacy.repository.UserRepository;
import com.medicare.pharmacy.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        Role role = resolveRegisterRole(request.getRole());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(role)
                .enabled(true)
                .build();
        User saved = userRepository.save(user);

        cartRepository.save(Cart.builder().user(saved).build());

        String token = jwtUtil.generateToken(saved);
        return new AuthResponse(token, "Bearer", UserDto.from(saved));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new BadRequestException("Account is disabled");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, "Bearer", UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotFoundException("User", "email", "anonymous");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private Role resolveRegisterRole(Object rawRole) {
        if (rawRole == null) {
            return Role.CUSTOMER;
        }
        String roleStr = String.valueOf(rawRole).trim();
        if (roleStr.isEmpty()) {
            return Role.CUSTOMER;
        }
        Role parsed;
        try {
            parsed = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role: " + roleStr);
        }
        if (parsed != Role.CUSTOMER) {
            throw new BadRequestException("Cannot self-register as " + parsed.name());
        }
        return Role.CUSTOMER;
    }
}
