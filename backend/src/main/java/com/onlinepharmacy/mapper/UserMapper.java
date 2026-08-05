package com.onlinepharmacy.mapper;

import com.onlinepharmacy.dto.user.UserResponse;
import com.onlinepharmacy.entity.ERole;
import com.onlinepharmacy.entity.User;
import java.util.Comparator;
import org.springframework.stereotype.Component;

/**
 * Maps {@link User} entities to API responses. The "primary role" is a stable
 * ordering used by the client to gate UI: ADMIN > PHARMACIST > CUSTOMER.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        ERole primary = user.getRoles().stream()
                .max(Comparator.comparingInt(r -> r.getName().ordinal()))
                .map(r -> r.getName())
                .orElse(ERole.CUSTOMER);
        return UserResponse.of(user.getId(), user.getName(), user.getEmail(),
                user.getPhone(), primary, user.isActive());
    }
}