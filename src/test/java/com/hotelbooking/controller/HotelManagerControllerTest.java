package com.hotelbooking.controller;

import com.hotelbooking.dto.RoomDTO;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.RoomType;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.service.HotelManagerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelManagerControllerTest {

    @Mock
    private HotelManagerService hotelManagerService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HotelManagerController hotelManagerController;

    @Test
    void dashboardShouldUseManagerIdFromNumericAuthenticationName() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("9");
        when(hotelManagerService.hotelExistsForManager(9L)).thenReturn(true);
        when(hotelManagerService.getBookingCount(9L)).thenReturn(5L);
        when(hotelManagerService.getAvailableRoomCount(9L)).thenReturn(12);

        Model model = new ExtendedModelMap();
        String view = hotelManagerController.dashboard(authentication, model);

        assertEquals("manager/dashboard", view);
        assertEquals(true, model.getAttribute("hasHotel"));
        assertEquals(5L, model.getAttribute("bookingCount"));
        assertEquals(12, model.getAttribute("availableRooms"));
    }

    @Test
    void viewRoomsShouldResolveManagerByEmailWhenAuthenticationNameIsNotNumeric() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com"))
                .thenReturn(Optional.of(User.builder().id(11L).email("manager@test.com").build()));

        List<RoomDTO> rooms = List.of(RoomDTO.builder().r_id(2L).roomType(RoomType.SINGLE).build());
        when(hotelManagerService.getRooms(11L)).thenReturn(rooms);

        Model model = new ExtendedModelMap();
        String view = hotelManagerController.viewRooms(authentication, model);

        assertEquals("manager/ManageRoom", view);
        assertEquals(rooms, model.getAttribute("rooms"));
    }

    @Test
    void addRoomShouldCallServiceAndRedirect() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("15");
        RoomDTO roomDTO = RoomDTO.builder()
                .roomType(RoomType.DELUXE)
                .pricePerNight(300.0)
                .totalRooms(3)
                .build();

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        String view = hotelManagerController.addRoom(roomDTO, authentication, ra);

        verify(hotelManagerService).addRoom(roomDTO, 15L);
        assertEquals("redirect:/manager/rooms", view);
        assertEquals("Room added successfully", ra.getFlashAttributes().get("success"));
    }
}
