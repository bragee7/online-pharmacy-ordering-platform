package com.onlinepharmacy.dto.auth;

import com.onlinepharmacy.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT login/registration response")
public record LoginResponse(
        @Schema(description = "JWT bearer token (24h validity)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,
        @Schema(description = "Token type, always 'Bearer'", example = "Bearer")
        String tokenType,
        @Schema(description = "Authenticated user summary")
        UserResponse user
) {
    public static LoginResponse of(String token, UserResponse user) {
        return new LoginResponse(token, "Bearer", user);
    }
}