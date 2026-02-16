package com.hotelbooking.mapper;

import com.hotelbooking.dto.UserDTO;
import com.hotelbooking.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    /* DTO → Entity (REGISTER) */
    public User toEntity(UserDTO dto) {
        if (dto == null) return null;

        return User.builder()
                .id(dto.getU_id()) // null during register (AUTO-GENERATED)
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .enabled(dto.isEnabled())
                .build();
    }

    public UserDTO toDTO(User user) {
        if (user == null) return null;

        return UserDTO.builder()
                .u_id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}
