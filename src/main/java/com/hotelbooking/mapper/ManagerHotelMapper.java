package com.hotelbooking.mapper;

import com.hotelbooking.dto.ManagerDTO;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManagerHotelMapper {

    public User toUserEntity(ManagerDTO dto) {
        return User.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .enabled(dto.isEnabled())
                .build();
    }

    public Hotel toHotelEntity(ManagerDTO dto) {
        return Hotel.builder()
                .hotelName(dto.getHotelName())
                .city(dto.getCity())
                .description(dto.getDescription())
                .mapsURL(dto.getMapsURL())
                .build();
    }
}
