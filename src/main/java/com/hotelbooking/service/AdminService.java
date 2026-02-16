package com.hotelbooking.service;

import com.hotelbooking.entity.*;
import java.util.*;

public interface AdminService {

    void approveHotelManager(Long userId);

    void deleteHotelManager(Long userId);

    List<User> getAllUsers();

    List<Hotel> getAllHotels();

    Hotel getHotelDetails(Long hotelId);

    List<Room> getHotelRooms(Long hotelId);

    List<Booking> getAllBookings();

    double getTotalRevenue();
}