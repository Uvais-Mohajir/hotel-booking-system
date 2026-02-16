package com.hotelbooking.repository;

import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.Role;
import com.hotelbooking.enums.RoomType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoomRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void findByHotelIdAndAvailableRoomsTrueShouldReturnOnlyAvailableRooms() {
        Hotel hotel = persistHotel("room-manager@test.com");

        entityManager.persist(Room.builder()
                .roomType(RoomType.DELUXE)
                .pricePerNight(250.0)
                .totalRooms(3)
                .availableRooms(true)
                .hotel(hotel)
                .build());
        entityManager.persist(Room.builder()
                .roomType(RoomType.SINGLE)
                .pricePerNight(100.0)
                .totalRooms(0)
                .availableRooms(false)
                .hotel(hotel)
                .build());
        entityManager.flush();

        List<Room> rooms = roomRepository.findByHotel_IdAndAvailableRoomsTrue(hotel.getId());

        assertEquals(1, rooms.size());
        assertEquals(RoomType.DELUXE, rooms.getFirst().getRoomType());
    }

    @Test
    void existsByHotelIdAndAvailabilityAndTotalRoomsShouldRespectThreshold() {
        Hotel hotel = persistHotel("threshold-manager@test.com");

        entityManager.persist(Room.builder()
                .roomType(RoomType.SUITE)
                .pricePerNight(450.0)
                .totalRooms(5)
                .availableRooms(true)
                .hotel(hotel)
                .build());
        entityManager.flush();

        boolean existsAtLeastOne = roomRepository.existsByHotel_IdAndAvailableRoomsTrueAndTotalRoomsGreaterThan(
                hotel.getId(), 1);
        boolean existsAboveSix = roomRepository.existsByHotel_IdAndAvailableRoomsTrueAndTotalRoomsGreaterThan(
                hotel.getId(), 6);

        assertTrue(existsAtLeastOne);
        assertFalse(existsAboveSix);
    }

    private Hotel persistHotel(String managerEmail) {
        User manager = User.builder()
                .email(managerEmail)
                .password("pass")
                .fullName("Manager")
                .role(Role.ROLE_HOTEL_MANAGER)
                .enabled(true)
                .build();
        entityManager.persist(manager);

        Hotel hotel = Hotel.builder()
                .hotelName("Room Hotel")
                .city("Pune")
                .description("Good description")
                .mapsURL("maps")
                .manager(manager)
                .build();
        entityManager.persist(hotel);
        return hotel;
    }
}
