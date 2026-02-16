package com.hotelbooking.service.impl;

import com.hotelbooking.dto.BookingDTO;
import com.hotelbooking.dto.PaymentDTO;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.BookingStatus;
import com.hotelbooking.mapper.BookingMapper;
import com.hotelbooking.mapper.PaymentMapper;
import com.hotelbooking.mapper.RoomMapper;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.PaymentRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RoomMapper roomMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void bookRoomShouldCreateConfirmedBookingWithTotalAmount() {
        User user = User.builder().id(1L).build();
        Hotel hotel = Hotel.builder().id(2L).build();
        Room room = Room.builder()
                .id(3L)
                .hotel(hotel)
                .availableRooms(true)
                .totalRooms(5)
                .pricePerNight(200.0)
                .build();

        BookingDTO bookingDTO = BookingDTO.builder()
                .hotelId(2L)
                .roomId(3L)
                .checkIn(LocalDateTime.of(2026, 2, 20, 14, 0))
                .checkOut(LocalDateTime.of(2026, 2, 23, 10, 0))
                .build();

        Booking mappedBooking = Booking.builder()
                .user(user)
                .hotel(hotel)
                .room(room)
                .checkIn(bookingDTO.getCheckIn())
                .checkOut(bookingDTO.getCheckOut())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hotelRepository.findById(2L)).thenReturn(Optional.of(hotel));
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(bookingRepository.countByRoom_IdAndStatusNotAndCheckOutAfterAndCheckInBefore(
                eq(3L), eq(BookingStatus.CANCELLED), eq(bookingDTO.getCheckIn()), eq(bookingDTO.getCheckOut())))
                .thenReturn(0L);
        when(bookingMapper.toEntity(bookingDTO, user, hotel, room)).thenReturn(mappedBooking);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        Long bookingId = customerService.bookRoom(bookingDTO, 1L);

        assertEquals(99L, bookingId);
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getValue();
        assertEquals(BookingStatus.CONFIRMED, savedBooking.getStatus());
        assertEquals(600.0, savedBooking.getTAmount());
    }

    @Test
    void bookRoomShouldFailWhenCheckOutIsNotAfterCheckIn() {
        User user = User.builder().id(1L).build();
        Hotel hotel = Hotel.builder().id(2L).build();
        Room room = Room.builder()
                .id(3L)
                .hotel(hotel)
                .availableRooms(true)
                .totalRooms(5)
                .pricePerNight(200.0)
                .build();

        BookingDTO bookingDTO = BookingDTO.builder()
                .hotelId(2L)
                .roomId(3L)
                .checkIn(LocalDateTime.of(2026, 2, 24, 14, 0))
                .checkOut(LocalDateTime.of(2026, 2, 23, 10, 0))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hotelRepository.findById(2L)).thenReturn(Optional.of(hotel));
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.bookRoom(bookingDTO, 1L));

        assertTrue(exception.getMessage().contains("Check-out must be after check-in"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void viewPaymentDetailsShouldReturnMappedDtoWhenPaymentExists() {
        User user = User.builder().id(1L).build();
        Booking booking = Booking.builder().id(50L).user(user).build();
        com.hotelbooking.entity.Payment payment = com.hotelbooking.entity.Payment.builder().id(7L).booking(booking).build();
        PaymentDTO paymentDTO = PaymentDTO.builder().p_id(7L).bookingId(50L).build();

        when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_Id(50L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(paymentDTO);

        PaymentDTO result = customerService.viewPaymentDetails(50L, 1L);

        assertEquals(7L, result.getP_id());
        assertEquals(50L, result.getBookingId());
    }

    @Test
    void bookRoomShouldFailWhenOverlappingBookingsReachTotalRooms() {
        User user = User.builder().id(1L).build();
        Hotel hotel = Hotel.builder().id(2L).build();
        Room room = Room.builder()
                .id(3L)
                .hotel(hotel)
                .availableRooms(true)
                .totalRooms(2)
                .pricePerNight(200.0)
                .build();

        BookingDTO bookingDTO = BookingDTO.builder()
                .hotelId(2L)
                .roomId(3L)
                .checkIn(LocalDateTime.of(2026, 2, 20, 14, 0))
                .checkOut(LocalDateTime.of(2026, 2, 23, 10, 0))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hotelRepository.findById(2L)).thenReturn(Optional.of(hotel));
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(bookingRepository.countByRoom_IdAndStatusNotAndCheckOutAfterAndCheckInBefore(
                eq(3L), eq(BookingStatus.CANCELLED), eq(bookingDTO.getCheckIn()), eq(bookingDTO.getCheckOut())))
                .thenReturn(2L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.bookRoom(bookingDTO, 1L));

        assertTrue(exception.getMessage().contains("already booked"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
