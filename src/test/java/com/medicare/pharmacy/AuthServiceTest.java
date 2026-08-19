package com.medicare.pharmacy;

import com.medicare.pharmacy.dto.AuthResponse;
import com.medicare.pharmacy.dto.LoginRequest;
import com.medicare.pharmacy.dto.RegisterRequest;
import com.medicare.pharmacy.entity.Cart;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.exception.BadRequestException;
import com.medicare.pharmacy.repository.CartRepository;
import com.medicare.pharmacy.repository.UserRepository;
import com.medicare.pharmacy.security.JwtUtil;
import com.medicare.pharmacy.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success_returnsToken() {
        RegisterRequest req = RegisterRequest.builder()
                .name("Test User")
                .email("new@medicare.com")
                .password("password")
                .phone("123")
                .address("Some Street")
                .role("CUSTOMER")
                .build();

        when(userRepository.existsByEmail("new@medicare.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        User saved = User.builder()
                .id(1L)
                .name("Test User")
                .email("new@medicare.com")
                .password("encoded")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken(saved)).thenReturn("jwt-token");

        AuthResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("jwt-token", res.getToken());
        assertEquals("Bearer", res.getTokenType());
        assertEquals("new@medicare.com", res.getUser().getEmail());
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        RegisterRequest req = RegisterRequest.builder()
                .name("Dup")
                .email("dup@medicare.com")
                .password("password")
                .role("CUSTOMER")
                .build();

        when(userRepository.existsByEmail("dup@medicare.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void login_invalidPassword_throwsBadRequest() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("user@medicare.com")
                .password("encoded")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        when(userRepository.findByEmail("user@medicare.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        LoginRequest req = LoginRequest.builder()
                .email("user@medicare.com")
                .password("wrong")
                .build();

        assertThrows(BadRequestException.class, () -> authService.login(req));
    }
}
