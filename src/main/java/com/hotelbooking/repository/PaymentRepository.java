package com.hotelbooking.repository;

import com.hotelbooking.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBooking_Id(Long bookingId);
}