package com.onlinepharmacy.service;

import com.onlinepharmacy.dto.auth.LoginRequest;
import com.onlinepharmacy.dto.auth.LoginResponse;
import com.onlinepharmacy.dto.auth.RegisterRequest;
import com.onlinepharmacy.dto.user.UserResponse;
import com.onlinepharmacy.entity.Cart;
import com.onlinepharmacy.entity.ERole;
import com.onlinepharmacy.entity.Role;
import com.onlinepharmacy.entity.User;
import com.onlinepharmacy.exception.DuplicateResourceException;
import com.onlinepharmacy.mapper.UserMapper;
import com.onlinepharmacy.repository.CartRepository;
import com.onlinepharmacy.repository.RoleRepository;
import com.onlinepharmacy.repository.UserRepository;
import com.onlinepharmacy.security.JwtService;
import com.onlinepharmacy.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       CartRepository cartRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.addRole(requiredRole(ERole.CUSTOMER));
        userRepository.save(user);

        cartRepository.save(new Cart(user));

        String token = jwtService.generateToken(user);
        log.info("New customer registered: {}", user.getEmail());
        return LoginResponse.of(token, userMapper.toResponse(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmailWithRoles(principal.getUsername())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));
        String token = jwtService.generateToken(user);
        return LoginResponse.of(token, userMapper.toResponse(user));
    }

    private Role requiredRole(ERole name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Seed role missing: " + name));
    }
}