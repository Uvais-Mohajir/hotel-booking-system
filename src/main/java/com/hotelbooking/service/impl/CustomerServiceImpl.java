package com.hotelbooking.service.impl;

import com.hotelbooking.dto.*;
import com.hotelbooking.entity.*;
import com.hotelbooking.enums.*;
import com.hotelbooking.mapper.*;
import com.hotelbooking.repository.*;
import com.hotelbooking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    private final RoomMapper roomMapper;
    private final BookingMapper bookingMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public List<Hotel> getFeaturedHotels() {
        return hotelRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .filter(hotel -> roomRepository.existsByHotel_IdAndAvailableRoomsTrueAndTotalRoomsGreaterThan(
                        hotel.getId(), 0))
                .limit(4)
                .toList();
    }

    // ================= SEARCH =================
    @Override
    public List<Hotel> searchHotels(String city) {
        return hotelRepository.findByCityIgnoreCase(city);
    }

    // ================= HOTEL DETAILS =================
    @Override
    public Hotel viewHotelDetails(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    // ================= ROOM DETAILS =================
    @Override
    public List<RoomDTO> viewAvailableRooms(Long hotelId) {

        return roomRepository.findByHotel_IdAndAvailableRoomsTrue(hotelId)
                .stream()
                .map(roomMapper::toDTO)
                .toList();
    }

    // ================= BOOK ROOM =================
    @Override
    public Long bookRoom(BookingDTO bookingDTO, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Hotel hotel = hotelRepository.findById(bookingDTO.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        Room room = roomRepository.findById(bookingDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isAvailableRooms() || room.getTotalRooms() <= 0) {
            throw new RuntimeException("Room not available");
        }

        if (bookingDTO.getCheckIn() == null || bookingDTO.getCheckOut() == null) {
            throw new RuntimeException("Check-in and check-out are required");
        }
        if (!bookingDTO.getCheckIn().isBefore(bookingDTO.getCheckOut())) {
            throw new RuntimeException("Check-out must be after check-in");
        }
        if (!bookingDTO.getCheckOut().toLocalDate().isAfter(bookingDTO.getCheckIn().toLocalDate())) {
            throw new RuntimeException("Booking must be at least 1 night");
        }
        if (room.getHotel() == null || !room.getHotel().getId().equals(hotel.getId())) {
            throw new RuntimeException("Selected room does not belong to this hotel");
        }

        long overlappingBookings = bookingRepository.countByRoom_IdAndStatusNotAndCheckOutAfterAndCheckInBefore(
                room.getId(),
                BookingStatus.CANCELLED,
                bookingDTO.getCheckIn(),
                bookingDTO.getCheckOut()
        );
        if (overlappingBookings >= room.getTotalRooms()) {
            throw new RuntimeException("Selected room is already booked for given dates");
        }

        long nights = ChronoUnit.DAYS.between(
                bookingDTO.getCheckIn().toLocalDate(),
                bookingDTO.getCheckOut().toLocalDate()
        );
        double totalAmount = nights * room.getPricePerNight();

        Booking booking = bookingMapper.toEntity(bookingDTO, user, hotel, room);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTAmount(totalAmount);

        return bookingRepository.save(booking).getId();
    }

    // ================= VIEW BOOKING =================
    @Override
    public BookingDTO viewBooking(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return bookingMapper.toDTO(booking);
    }

    // ================= VIEW USER BOOKINGS =================
    @Override
    public List<BookingDTO> viewMyBookings(Long userId) {

        return bookingRepository.findByUser_Id(userId)
                .stream()
                .map(bookingMapper::toDTO)
                .toList();
    }

    // ================= CANCEL BOOKING =================
    @Override
    public void cancelBooking(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // ================= PAYMENT DETAILS =================
    @Override
    public PaymentDTO viewPaymentDetails(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return paymentRepository.findByBooking_Id(bookingId)
                .map(paymentMapper::toDTO)
                .orElse(null);
    }
}
