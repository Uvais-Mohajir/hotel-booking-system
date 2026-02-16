package com.hotelbooking.repository;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.BookingStatus;
import com.hotelbooking.enums.Role;
import com.hotelbooking.enums.RoomType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void existsByRoomAndDateRangeShouldReturnTrueForOverlap() {
        TestData data = persistBookingGraph();

        boolean exists = bookingRepository.existsByRoom_IdAndCheckOutAfterAndCheckInBefore(
                data.room().getId(),
                LocalDateTime.of(2026, 3, 2, 12, 0),
                LocalDateTime.of(2026, 3, 6, 10, 0)
        );

        assertTrue(exists);
    }

    @Test
    void existsByRoomAndDateRangeShouldReturnFalseForNonOverlap() {
        TestData data = persistBookingGraph();

        boolean exists = bookingRepository.existsByRoom_IdAndCheckOutAfterAndCheckInBefore(
                data.room().getId(),
                LocalDateTime.of(2026, 3, 7, 12, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0)
        );

        assertFalse(exists);
    }

    private TestData persistBookingGraph() {
        User manager = User.builder()
                .email("manager@test.com")
                .password("pass")
                .fullName("Manager")
                .role(Role.ROLE_HOTEL_MANAGER)
                .enabled(true)
                .build();
        entityManager.persist(manager);

        User customer = User.builder()
                .email("user@test.com")
                .password("pass")
                .fullName("Customer")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
        entityManager.persist(customer);

        Hotel hotel = Hotel.builder()
                .hotelName("Sunrise")
                .city("Pune")
                .description("Hotel Desc")
                .mapsURL("maps-url")
                .manager(manager)
                .build();
        entityManager.persist(hotel);

        Room room = Room.builder()
                .roomType(RoomType.DELUXE)
                .pricePerNight(150.0)
                .totalRooms(10)
                .availableRooms(true)
                .hotel(hotel)
                .build();
        entityManager.persist(room);

        Booking booking = Booking.builder()
                .checkIn(LocalDateTime.of(2026, 3, 1, 14, 0))
                .checkOut(LocalDateTime.of(2026, 3, 5, 10, 0))
                .status(BookingStatus.CONFIRMED)
                .tAmount(600.0)
                .user(customer)
                .hotel(hotel)
                .room(room)
                .build();
        entityManager.persist(booking);

        entityManager.flush();
        return new TestData(room, booking);
    }

    private record TestData(Room room, Booking booking) {
    }
}
