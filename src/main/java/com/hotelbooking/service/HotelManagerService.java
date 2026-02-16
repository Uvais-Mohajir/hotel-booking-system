package com.hotelbooking.service;

import com.hotelbooking.dto.*;
import java.util.*;

public interface HotelManagerService {

    boolean hotelExistsForManager(Long managerId);

    long getBookingCount(Long managerId);

    int getAvailableRoomCount(Long managerId);

    void addRoom(RoomDTO roomDTO, Long managerId);

    void updateRoom(RoomDTO roomDTO, Long managerId);

    List<RoomDTO> getRooms(Long managerId);

    List<BookingDTO> getHotelBookings(Long managerId);

    double getHotelBookingTotalAmount(Long managerId);
}
