package com.hotelbooking.service.impl;

import com.hotelbooking.entity.*;
import com.hotelbooking.enums.*;
import com.hotelbooking.exception.AuthenticationFailedException;
import com.hotelbooking.repository.*;
import com.hotelbooking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ================= USER REGISTER =================
    @Override
    public User registerUser(User user, Role role) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        user.setRole(role);
        user.setEnabled(true); // user can login immediately
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // ================= HOTEL MANAGER REGISTER =================
    @Override
    public User registerHotelManager(User user, Hotel hotel, Role role) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // manager details
        user.setRole(role);
        user.setEnabled(false); // ADMIN APPROVAL REQUIRED
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedManager = userRepository.save(user);

        // link hotel with manager
        hotel.setManager(savedManager);
        hotelRepository.save(hotel);

        return savedManager;
    }

    // ================= LOGIN =================
    @Override
    public User login(String email, String password, Role role) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (AuthenticationException ex) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        // IMPORTANT: Store authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AuthenticationFailedException("User not found"));

        /* ROLE CHECK (Admin / User separation) */
        if (!user.getRole().equals(role)) {
            SecurityContextHolder.clearContext();
            throw new AuthenticationFailedException("Invalid role login attempt");
        }

        /* ADMIN APPROVAL */
        if (!user.isEnabled()) {
            SecurityContextHolder.clearContext();
            throw new AuthenticationFailedException("Account not approved by admin");
        }

        return user;
    }
}