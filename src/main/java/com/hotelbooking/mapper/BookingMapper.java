package com.hotelbooking.mapper;

import com.hotelbooking.dto.BookingDTO;
import com.hotelbooking.entity.*;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public Booking toEntity(
            BookingDTO dto,
            User user,
            Hotel hotel,
            Room room
    ) {
        return Booking.builder()
                .id(dto.getB_id())
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .status(dto.getStatus())
                .tAmount(dto.getTAmount())
                .user(user)
                .hotel(hotel)
                .room(room)
                .build();
    }

    public BookingDTO toDTO(Booking booking) {
        return BookingDTO.builder()
                .b_id(booking.getId())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .status(booking.getStatus())
                .tAmount(booking.getTAmount())
                .userId(booking.getUser().getId())
                .hotelId(booking.getHotel().getId())
                .roomId(booking.getRoom().getId())
                .build();
    }
}
