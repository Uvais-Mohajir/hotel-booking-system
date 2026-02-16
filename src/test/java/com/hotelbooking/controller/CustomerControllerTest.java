package com.hotelbooking.controller;

import com.hotelbooking.dto.BookingDTO;
import com.hotelbooking.dto.PaymentDTO;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.PaymentStatus;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.service.CustomerService;
import com.hotelbooking.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(customerController, "razorpayKeyId", "rzp_test_key");
    }

    @Test
    void searchHotelsShouldUseCityFilterWhenProvided() {
        Model model = new ExtendedModelMap();
        List<Hotel> hotels = List.of(Hotel.builder().id(1L).city("Delhi").build());
        when(customerService.searchHotels("Delhi")).thenReturn(hotels);

        String viewName = customerController.searchHotels("Delhi", model);

        assertEquals("user/searchHotelsViewHotels", viewName);
        assertSame(hotels, model.getAttribute("hotels"));
    }

    @Test
    void bookRoomShouldRedirectToPaymentPage() {
        BookingDTO bookingDTO = BookingDTO.builder().hotelId(2L).roomId(3L).build();
        org.springframework.security.core.Authentication authentication =
                mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("5");
        when(customerService.bookRoom(bookingDTO, 5L)).thenReturn(88L);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String viewName = customerController.bookRoom(bookingDTO, authentication, redirectAttributes);

        assertEquals("redirect:/user/bookings/88/payment", viewName);
        assertEquals("Room reserved. Complete payment to confirm.",
                redirectAttributes.getFlashAttributes().get("success"));
    }

    @Test
    void paymentPageShouldCreateOrderWhenExistingPaymentIsMissing() {
        org.springframework.security.core.Authentication authentication =
                mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(User.builder().id(7L).email("user@example.com").build()));

        BookingDTO booking = BookingDTO.builder().b_id(10L).hotelId(2L).roomId(3L).build();
        PaymentDTO payment = PaymentDTO.builder()
                .p_id(100L)
                .bookingId(10L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        when(customerService.viewBooking(10L, 7L)).thenReturn(booking);
        when(customerService.viewPaymentDetails(10L, 7L)).thenReturn(null);
        when(paymentService.createRazorpayOrder(10L, 7L)).thenReturn(payment);

        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String viewName = customerController.paymentPage(10L, authentication, model, redirectAttributes);

        assertEquals("user/payment", viewName);
        assertSame(booking, model.getAttribute("booking"));
        assertSame(payment, model.getAttribute("payment"));
        assertEquals("rzp_test_key", model.getAttribute("razorpayKeyId"));
        verify(paymentService).createRazorpayOrder(10L, 7L);
    }
}
