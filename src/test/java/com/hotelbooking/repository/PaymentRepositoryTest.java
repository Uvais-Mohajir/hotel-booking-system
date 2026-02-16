package com.hotelbooking.repository;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.BookingStatus;
import com.hotelbooking.enums.PaymentStatus;
import com.hotelbooking.enums.Role;
import com.hotelbooking.enums.RoomType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void findByBookingIdShouldReturnPaymentWhenPresent() {
        Booking booking = persistBookingGraph();
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(300.0)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentReferenceId("order|payment")
                .paymentProvider("RAZORPAY")
                .build();
        entityManager.persist(payment);
        entityManager.flush();

        Optional<Payment> found = paymentRepository.findByBooking_Id(booking.getId());

        assertTrue(found.isPresent());
        assertEquals("RAZORPAY", found.get().getPaymentProvider());
    }

    private Booking persistBookingGraph() {
        User manager = User.builder()
                .email("manager-pay@test.com")
                .password("pass")
                .fullName("Manager")
                .role(Role.ROLE_HOTEL_MANAGER)
                .enabled(true)
                .build();
        entityManager.persist(manager);

        User customer = User.builder()
                .email("customer-pay@test.com")
                .password("pass")
                .fullName("Customer")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
        entityManager.persist(customer);

        Hotel hotel = Hotel.builder()
                .hotelName("Pay Hotel")
                .city("Mumbai")
                .description("Pay hotel description")
                .mapsURL("maps")
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
                .checkIn(LocalDateTime.of(2026, 4, 1, 14, 0))
                .checkOut(LocalDateTime.of(2026, 4, 3, 10, 0))
                .status(BookingStatus.CONFIRMED)
                .tAmount(300.0)
                .user(customer)
                .hotel(hotel)
                .room(room)
                .build();
        entityManager.persist(booking);

        return booking;
    }
}
