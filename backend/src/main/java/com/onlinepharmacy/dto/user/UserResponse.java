package com.onlinepharmacy.dto.user;

import com.onlinepharmacy.entity.ERole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User summary returned by the API (never includes the password)")
public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        @Schema(description = "Primary role for the UI, e.g. CUSTOMER")
        ERole role,
        boolean active
) {

    public static UserResponse of(Long id, String name, String email, String phone, ERole primaryRole, boolean active) {
        return new UserResponse(id, name, email, phone, primaryRole, active);
    }
}