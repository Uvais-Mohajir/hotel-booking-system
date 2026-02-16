package com.hotelbooking.repository;

import com.hotelbooking.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCityIgnoreCase(String city);

    Optional<Hotel> findByManager_Id(Long managerId);

    Optional<Hotel> findById(Long h_id);

    List<Hotel> findTop4ByOrderByIdDesc();
}
