package com.hotelbooking.service.impl;

import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.Role;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.PaymentRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void approveHotelManagerShouldEnableManager() {
        User manager = User.builder().id(7L).role(Role.ROLE_HOTEL_MANAGER).enabled(false).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(manager));

        adminService.approveHotelManager(7L);

        assertEquals(true, manager.isEnabled());
        verify(userRepository).save(manager);
    }

    @Test
    void approveHotelManagerShouldFailForNonManagerRole() {
        User user = User.builder().id(7L).role(Role.ROLE_USER).enabled(false).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.approveHotelManager(7L));

        assertEquals("User is not a Hotel Manager", ex.getMessage());
    }

    @Test
    void getTotalRevenueShouldSumPaymentAmounts() {
        when(paymentRepository.findAll()).thenReturn(List.of(
                Payment.builder().amount(100.0).build(),
                Payment.builder().amount(250.5).build()
        ));

        double total = adminService.getTotalRevenue();

        assertEquals(350.5, total);
    }

    @Test
    void deleteHotelManagerShouldDeleteHotelAndManager() {
        User manager = User.builder().id(5L).role(Role.ROLE_HOTEL_MANAGER).build();
        Hotel hotel = Hotel.builder().id(9L).manager(manager).build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(manager));
        when(hotelRepository.findByManager_Id(5L)).thenReturn(Optional.of(hotel));

        adminService.deleteHotelManager(5L);

        verify(hotelRepository).delete(hotel);
        verify(userRepository).delete(manager);
    }
}
