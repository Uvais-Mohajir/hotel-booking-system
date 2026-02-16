package com.hotelbooking.service;

import com.hotelbooking.entity.*;
import com.hotelbooking.enums.*;

public interface AuthService {

    User registerUser(User user, Role role);

    User registerHotelManager(User user, Hotel hotel, Role role);

    User login(String email, String password, Role role);
}