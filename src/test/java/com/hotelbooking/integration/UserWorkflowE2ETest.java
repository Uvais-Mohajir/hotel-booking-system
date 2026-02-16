package com.hotelbooking.integration;

import com.hotelbooking.dto.BookingDTO;
import com.hotelbooking.dto.PaymentDTO;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.BookingStatus;
import com.hotelbooking.enums.Role;
import com.hotelbooking.enums.RoomType;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserWorkflowE2ETest {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void userBookingWorkflowShouldBookViewListAndCancel() {
        User manager = userRepository.save(User.builder()
                .email("manager.workflow@test.com")
                .password("pass")
                .fullName("Manager Workflow")
                .role(Role.ROLE_HOTEL_MANAGER)
                .enabled(true)
                .build());

        User user = userRepository.save(User.builder()
                .email("user.workflow@test.com")
                .password("pass")
                .fullName("User Workflow")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build());

        Hotel hotel = hotelRepository.save(Hotel.builder()
                .hotelName("Workflow Stay")
                .city("Bangalore")
                .description("Workflow hotel")
                .mapsURL("maps")
                .manager(manager)
                .build());

        Room room = roomRepository.save(Room.builder()
                .roomType(RoomType.DELUXE)
                .pricePerNight(300.0)
                .totalRooms(5)
                .availableRooms(true)
                .hotel(hotel)
                .build());

        BookingDTO bookingRequest = BookingDTO.builder()
                .hotelId(hotel.getId())
                .roomId(room.getId())
                .checkIn(LocalDateTime.of(2026, 4, 10, 14, 0))
                .checkOut(LocalDateTime.of(2026, 4, 12, 10, 0))
                .build();

        Long bookingId = customerService.bookRoom(bookingRequest, user.getId());
        assertNotNull(bookingId);

        BookingDTO booking = customerService.viewBooking(bookingId, user.getId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(600.0, booking.getTAmount());

        List<BookingDTO> bookings = customerService.viewMyBookings(user.getId());
        assertEquals(1, bookings.size());

        PaymentDTO payment = customerService.viewPaymentDetails(bookingId, user.getId());
        assertNull(payment);

        customerService.cancelBooking(bookingId, user.getId());
        assertEquals(
                BookingStatus.CANCELLED,
                bookingRepository.findById(bookingId).orElseThrow().getStatus()
        );
    }
}
