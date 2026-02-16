package com.hotelbooking.mapper;

import com.hotelbooking.dto.RoomDTO;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public Room toEntity(RoomDTO dto, Hotel hotel) {
        return Room.builder()
                .id(dto.getR_id())
                .roomType(dto.getRoomType())
                .pricePerNight(dto.getPricePerNight())
                .totalRooms(dto.getTotalRooms())
                .availableRooms(dto.isAvailableRooms())
                .hotel(hotel)
                .build();
    }

    public RoomDTO toDTO(Room room) {
        return RoomDTO.builder()
                .r_id(room.getId())
                .roomType(room.getRoomType())
                .pricePerNight(room.getPricePerNight())
                .totalRooms(room.getTotalRooms())
                .availableRooms(room.isAvailableRooms())
                .hotelId(room.getHotel().getId())
                .build();
    }
}
