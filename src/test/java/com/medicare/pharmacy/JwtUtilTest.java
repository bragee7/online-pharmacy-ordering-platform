package com.medicare.pharmacy;

import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.enums.Role;
import com.medicare.pharmacy.security.JwtProperties;
import com.medicare.pharmacy.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF-test-secret!");
        props.setExpiration(86400000L);
        jwtUtil = new JwtUtil(props);
    }

    @Test
    void generateAndExtractEmail() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@medicare.com")
                .password("x")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertEquals("test@medicare.com", jwtUtil.extractEmail(token));
        assertEquals("CUSTOMER", jwtUtil.extractRole(token));
        assertEquals(1L, jwtUtil.extractUserId(token));
    }

    @Test
    void isTokenValid_matchingUser() {
        User user = User.builder()
                .id(2L)
                .name("Jane")
                .email("jane@medicare.com")
                .password("x")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        String token = jwtUtil.generateToken(user);

        UserDetails details = org.springframework.security.core.userdetails.User.builder()
                .username("jane@medicare.com")
                .password("x")
                .authorities("ROLE_CUSTOMER")
                .build();

        assertTrue(jwtUtil.isTokenValid(token, details));
    }

    @Test
    void isTokenValid_wrongUser_returnsFalse() {
        User user = User.builder()
                .id(3L)
                .name("Bob")
                .email("bob@medicare.com")
                .password("x")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        String token = jwtUtil.generateToken(user);

        UserDetails other = org.springframework.security.core.userdetails.User.builder()
                .username("alice@medicare.com")
                .password("x")
                .authorities("ROLE_CUSTOMER")
                .build();

        assertFalse(jwtUtil.isTokenValid(token, other));
    }
}
