package com.hotelbooking.controller;

import com.hotelbooking.entity.Hotel;
import com.hotelbooking.repository.HotelRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HomeController homeController;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void indexShouldLoadLatestHotels() {
        List<Hotel> hotels = List.of(Hotel.builder().id(1L).hotelName("A").build());
        when(hotelRepository.findTop4ByOrderByIdDesc()).thenReturn(hotels);

        Model model = new ExtendedModelMap();
        String view = homeController.index(model);

        assertEquals("index", view);
        assertEquals(hotels, model.getAttribute("hotels"));
    }

    @Test
    void logoutShouldAlwaysRedirectToIndex() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("user", "pass"));

        String view = homeController.logout(mock(jakarta.servlet.http.HttpServletRequest.class),
                mock(jakarta.servlet.http.HttpServletResponse.class));

        assertEquals("redirect:/index", view);
    }
}
