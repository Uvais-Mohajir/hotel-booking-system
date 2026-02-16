package com.hotelbooking.service.impl;

import com.hotelbooking.dto.*;
import com.hotelbooking.entity.*;
import com.hotelbooking.mapper.*;
import com.hotelbooking.repository.*;
import com.hotelbooking.service.*;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelManagerServiceImpl implements HotelManagerService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final RoomMapper roomMapper;
    private final BookingMapper bookingMapper;

    @Override
    public boolean hotelExistsForManager(Long managerId) {
        return hotelRepository.findByManager_Id(managerId).isPresent();
    }

    @Override
    public long getBookingCount(Long managerId) {

        Hotel hotel = hotelRepository.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Hotel not found for manager"));

        return bookingRepository.findByHotel_Id(hotel.getId()).size();
    }

    // ================= AVAILABLE ROOM COUNT =================
    @Override
    public int getAvailableRoomCount(Long managerId) {

        Hotel hotel = hotelRepository.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Hotel not found for manager"));

        return roomRepository.findByHotel_IdAndAvailableRoomsTrue(hotel.getId())
                .stream()
                .mapToInt(Room::getTotalRooms)
                .sum();
    }

    // ================= ADD ROOM =================
    @Override
    public void addRoom(RoomDTO roomDTO, Long managerId) {

        Hotel hotel = hotelRepository.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Hotel not found for manager"));

        roomDTO.setAvailableRooms(roomDTO.getTotalRooms() > 0);
        Room room = roomMapper.toEntity(roomDTO, hotel);
        roomRepository.save(room);
    }

    @Override
    public void updateRoom(RoomDTO roomDTO, Long managerId) {

        if (roomDTO.getR_id() == null) {
            throw new RuntimeException("Room ID is required for update");
        }

        Hotel hotel = hotelRepository.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Hotel not found for manager"));

        Room room = roomRepository.findById(roomDTO.getR_id())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getHotel() == null || !room.getHotel().getId().equals(hotel.getId())) {
            throw new RuntimeException("You cannot update room from another hotel");
        }

        room.setRoomType(roomDTO.getRoomType());
        room.setPricePerNight(roomDTO.getPricePerNight());
        room.setTotalRooms(roomDTO.getTotalRooms());
        room.setAvailableRooms(roomDTO.getTotalRooms() > 0);

        roomRepository.save(room);
    }

    // ================= GET ROOMS =================
    @Override
    public List<RoomDTO> getRooms(Long managerId) {

        Hotel hotel = hotelRepository.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Hotel not found for manager"));

        return roomRepository.findByHotel_Id(hotel.getId())
                .stream()
                .map(roomMapper::toDTO)
                .toList();
    }

    // ================= GET BOOKINGS =================
    @Override
    public List<BookingDTO> getHotelBookings(Long managerId) {

        Hotel hotel = hotelRepository.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Hotel not found for manager"));

        return bookingRepository.findByHotel_Id(hotel.getId())
                .stream()
                .map(bookingMapper::toDTO)
                .toList();
    }

    @Override
    public double getHotelBookingTotalAmount(Long managerId) {
        Hotel hotel = hotelRepository.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Hotel not found for manager"));

        return bookingRepository.findByHotel_Id(hotel.getId())
                .stream()
                .mapToDouble(Booking::getTAmount)
                .sum();
    }
}
