package com.hotelbooking.repository;

import com.hotelbooking.entity.*;
import com.hotelbooking.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser_Id(Long userId);

    List<Booking> findByHotel_Id(Long hotelId);

    boolean existsByRoom_IdAndCheckOutAfterAndCheckInBefore(
            Long roomId,
            LocalDateTime checkIn,
            LocalDateTime checkOut
    );

    long countByRoom_IdAndStatusNotAndCheckOutAfterAndCheckInBefore(
            Long roomId,
            BookingStatus status,
            LocalDateTime checkIn,
            LocalDateTime checkOut
    );
}
