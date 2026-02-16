package com.hotelbooking.controller;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.User;
import com.hotelbooking.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void dashboardShouldPopulateSummaryValues() {
        when(adminService.getAllUsers()).thenReturn(List.of(User.builder().id(1L).build()));
        when(adminService.getAllHotels()).thenReturn(List.of(Hotel.builder().id(2L).build()));
        when(adminService.getAllBookings()).thenReturn(List.of(Booking.builder().id(3L).build()));
        when(adminService.getTotalRevenue()).thenReturn(999.5);

        Model model = new ExtendedModelMap();
        String view = adminController.dashboard(model);

        assertEquals("admin/dashboard", view);
        assertEquals(1, model.getAttribute("totalUsers"));
        assertEquals(1, model.getAttribute("totalHotels"));
        assertEquals(1, model.getAttribute("totalBookings"));
        assertEquals(999.5, model.getAttribute("totalRevenue"));
    }

    @Test
    void approveHotelManagerShouldRedirectWithSuccessMessage() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = adminController.approveHotelManager(5L, ra);

        verify(adminService).approveHotelManager(5L);
        assertEquals("redirect:/admin/users", view);
        assertEquals("Hotel manager approved successfully", ra.getFlashAttributes().get("success"));
    }
}
