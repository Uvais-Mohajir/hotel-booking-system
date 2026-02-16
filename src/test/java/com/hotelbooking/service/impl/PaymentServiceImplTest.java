package com.hotelbooking.service.impl;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.User;
import com.hotelbooking.mapper.PaymentMapper;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.PaymentRepository;
import com.hotelbooking.repository.UserRepository;
import com.razorpay.RazorpayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private RazorpayClient razorpayClient;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createRazorpayOrderShouldFailWhenBookingNotFound() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.createRazorpayOrder(10L, 1L));

        assertEquals("Booking not found", ex.getMessage());
    }

    @Test
    void createRazorpayOrderShouldFailWhenUserDoesNotOwnBooking() {
        Booking booking = Booking.builder().id(10L).user(User.builder().id(1L).build()).build();
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).build()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> paymentService.createRazorpayOrder(10L, 2L));

        assertEquals("Unauthorized payment access", ex.getMessage());
    }

    @Test
    void verifyAndSavePaymentShouldFailWhenUserDoesNotOwnBooking() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "test_secret");
        Booking booking = Booking.builder().id(55L).user(User.builder().id(4L).build()).build();
        when(bookingRepository.findById(55L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(8L)).thenReturn(Optional.of(User.builder().id(8L).build()));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                paymentService.verifyAndSavePayment(55L, "pay_1", "order_1", "sig_1", 8L));

        assertEquals("Unauthorized payment access", ex.getMessage());
    }
}
