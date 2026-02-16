package com.hotelbooking.service.impl;

import com.hotelbooking.entity.*;
import com.hotelbooking.enums.Role;
import com.hotelbooking.repository.*;
import com.hotelbooking.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    // ================= APPROVE HOTEL MANAGER =================
    @Override
    public void approveHotelManager(Long userId) {

        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != Role.ROLE_HOTEL_MANAGER) {
            throw new RuntimeException("User is not a Hotel Manager");
        }

        manager.setEnabled(true);
        userRepository.save(manager);
    }

    // ================= DELETE HOTEL MANAGER =================
    @Override
    public void deleteHotelManager(Long userId) {

        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != Role.ROLE_HOTEL_MANAGER) {
            throw new RuntimeException("User is not a Hotel Manager");
        }

        // Delete hotel if exists
        hotelRepository.findByManager_Id(userId)
                .ifPresent(hotelRepository::delete);

        userRepository.delete(manager);
    }

    // ================= GET ALL USERS =================
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ================= GET ALL HOTELS =================
    @Override
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    // ================= HOTEL DETAILS =================
    @Override
    public Hotel getHotelDetails(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    // ================= HOTEL ROOMS =================
    @Override
    public List<Room> getHotelRooms(Long hotelId) {
        return roomRepository.findByHotel_Id(hotelId);
    }

    // ================= ALL BOOKINGS =================
    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // ================= TOTAL REVENUE =================
    @Override
    public double getTotalRevenue() {
        return paymentRepository.findAll()
                .stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }
}