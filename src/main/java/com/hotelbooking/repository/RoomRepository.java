package com.hotelbooking.repository;

import com.hotelbooking.entity.*;
import com.hotelbooking.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotel_Id(Long hotelId);

    List<Room> findByHotel_IdAndAvailableRoomsTrue(Long hotelId);

    boolean existsByHotel_IdAndAvailableRoomsTrueAndTotalRoomsGreaterThan(Long hotelId, int totalRooms);
}
