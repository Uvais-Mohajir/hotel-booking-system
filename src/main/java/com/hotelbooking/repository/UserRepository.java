package com.hotelbooking.repository;

import com.hotelbooking.entity.*;
import com.hotelbooking.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}