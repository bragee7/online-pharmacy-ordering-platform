package com.onlinepharmacy.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Body used to create a new customer account")
public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        @Size(max = 120, message = "Email must be at most 120 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(regexp = ".*[A-Za-z].*", message = "Password must contain at least one letter")
        @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one digit")
        String password,

        @Size(max = 20, message = "Phone must be at most 20 characters")
        @Pattern(regexp = "^[0-9+\\- ]*$", message = "Phone can only contain digits, +, - or spaces")
        String phone
) {}