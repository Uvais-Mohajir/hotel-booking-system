package com.hotelbooking.service;

import com.hotelbooking.dto.*;
import com.hotelbooking.entity.*;
import java.util.*;

public interface CustomerService {

    List<Hotel> getFeaturedHotels();

    List<Hotel> searchHotels(String city);

    Hotel viewHotelDetails(Long hotelId);

    List<RoomDTO> viewAvailableRooms(Long hotelId);

    Long bookRoom(BookingDTO bookingDTO, Long userId);

    BookingDTO viewBooking(Long bookingId, Long userId);

    List<BookingDTO> viewMyBookings(Long userId);

    void cancelBooking(Long bookingId, Long userId);

    PaymentDTO viewPaymentDetails(Long bookingId, Long userId);
}
