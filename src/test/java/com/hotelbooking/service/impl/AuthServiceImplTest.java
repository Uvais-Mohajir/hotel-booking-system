package com.hotelbooking.service.impl;

import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.Role;
import com.hotelbooking.exception.AuthenticationFailedException;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerUserShouldEncodePasswordAndEnableUser() {
        User user = User.builder().email("u@test.com").password("raw").enabled(false).build();
        when(userRepository.existsByEmail("u@test.com")).thenReturn(false);
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = authService.registerUser(user, Role.ROLE_USER);

        assertEquals("encoded", saved.getPassword());
        assertEquals(Role.ROLE_USER, saved.getRole());
        assertEquals(true, saved.isEnabled());
    }

    @Test
    void registerHotelManagerShouldSaveHotelWithSavedManager() {
        User manager = User.builder().email("m@test.com").password("raw").build();
        Hotel hotel = Hotel.builder().hotelName("Hotel").city("Pune").description("Description ok").build();
        when(userRepository.existsByEmail("m@test.com")).thenReturn(false);
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        authService.registerHotelManager(manager, hotel, Role.ROLE_HOTEL_MANAGER);

        ArgumentCaptor<Hotel> hotelCaptor = ArgumentCaptor.forClass(Hotel.class);
        verify(hotelRepository).save(hotelCaptor.capture());
        assertEquals(42L, hotelCaptor.getValue().getManager().getId());
    }

    @Test
    void loginShouldFailOnRoleMismatch() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("u@test.com", "pass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(
                User.builder().email("u@test.com").role(Role.ROLE_USER).enabled(true).build()
        ));

        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class,
                () -> authService.login("u@test.com", "pass", Role.ROLE_ADMIN));

        assertEquals("Invalid role login attempt", ex.getMessage());
    }

    @Test
    void loginShouldFailWhenAuthenticationManagerRejectsCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("bad credentials") {
                });

        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class,
                () -> authService.login("u@test.com", "bad", Role.ROLE_USER));

        assertEquals("Invalid email or password", ex.getMessage());
    }
}
