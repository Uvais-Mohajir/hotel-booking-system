package com.hotelbooking.controller;

import com.hotelbooking.dto.BookingDTO;
import com.hotelbooking.dto.RoomDTO;
import com.hotelbooking.entity.User;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.service.HotelManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_HOTEL_MANAGER')")
@Tag(name = "Hotel Manager", description = "Hotel manager room and booking endpoints")
public class HotelManagerController {

    private final HotelManagerService hotelManagerService;
    private final UserRepository userRepository;

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    @Operation(summary = "View manager dashboard", description = "Loads manager dashboard metrics.")
    @ApiResponse(responseCode = "200", description = "Dashboard page loaded")
    public String dashboard(Authentication authentication, Model model) {

        Long managerId = getManagerId(authentication);

        model.addAttribute(
                "hasHotel",
                hotelManagerService.hotelExistsForManager(managerId)
        );
        model.addAttribute(
                "bookingCount",
                hotelManagerService.getBookingCount(managerId)
        );
        model.addAttribute(
                "availableRooms",
                hotelManagerService.getAvailableRoomCount(managerId)
        );

        return "manager/dashboard";
    }

    // ================= ADD ROOM =================
    @GetMapping("/rooms/add")
    @Operation(summary = "Add room page", description = "Returns add-room form for manager.")
    @ApiResponse(responseCode = "200", description = "Add room page loaded")
    public String addRoomPage(Model model) {
        model.addAttribute("roomDTO", new RoomDTO());
        return "manager/AddRoom";
    }

    @PostMapping("/rooms/add")
    @Operation(summary = "Add room", description = "Adds a room under manager's hotel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Room added and redirected"),
            @ApiResponse(responseCode = "400", description = "Invalid room payload", content = @Content(schema = @Schema()))
    })
    public String addRoom(
            @Parameter(description = "Room payload") @ModelAttribute RoomDTO roomDTO,
            @Parameter(hidden = true) Authentication authentication,
            RedirectAttributes ra
    ) {
        Long managerId = getManagerId(authentication);

        hotelManagerService.addRoom(roomDTO, managerId);
        ra.addFlashAttribute("success", "Room added successfully");

        return "redirect:/manager/rooms";
    }

    // ================= VIEW ROOMS =================
    @GetMapping("/rooms")
    @Operation(summary = "View rooms", description = "Lists all rooms managed by the manager.")
    @ApiResponse(responseCode = "200", description = "Rooms page loaded")
    public String viewRooms(Authentication authentication, Model model) {

        Long managerId = getManagerId(authentication);
        List<RoomDTO> rooms = hotelManagerService.getRooms(managerId);

        model.addAttribute("rooms", rooms);
        return "manager/ManageRoom";
    }

    @GetMapping("/rooms/update")
    @Operation(summary = "Update room page", description = "Returns update-room page and optionally preselects a room.")
    @ApiResponse(responseCode = "200", description = "Update room page loaded")
    public String updateRoomPage(
            @Parameter(description = "Optional room id to preselect", example = "20") @RequestParam(required = false) Long roomId,
            @Parameter(hidden = true) Authentication authentication,
            Model model
    ) {
        Long managerId = getManagerId(authentication);
        model.addAttribute("rooms", hotelManagerService.getRooms(managerId));
        model.addAttribute("selectedRoomId", roomId);
        return "manager/UpdateRoom";
    }

    @PostMapping("/rooms/update")
    @Operation(summary = "Update room", description = "Updates room details for manager's hotel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Room updated and redirected"),
            @ApiResponse(responseCode = "400", description = "Invalid room payload", content = @Content(schema = @Schema()))
    })
    public String updateRoom(
            @Parameter(description = "Room payload") @ModelAttribute RoomDTO roomDTO,
            @Parameter(hidden = true) Authentication authentication,
            RedirectAttributes ra
    ) {
        Long managerId = getManagerId(authentication);
        hotelManagerService.updateRoom(roomDTO, managerId);
        ra.addFlashAttribute("success", "Room updated successfully");
        return "redirect:/manager/rooms/update";
    }

    // ================= VIEW BOOKINGS =================
    @GetMapping("/bookings")
    @Operation(summary = "View hotel bookings", description = "Lists bookings and total booking amount for manager's hotel.")
    @ApiResponse(responseCode = "200", description = "Bookings page loaded")
    public String viewBookings(Authentication authentication, Model model) {

        Long managerId = getManagerId(authentication);
        List<BookingDTO> bookings =
                hotelManagerService.getHotelBookings(managerId);

        model.addAttribute("bookings", bookings);
        model.addAttribute(
                "hotelTotalAmount",
                hotelManagerService.getHotelBookingTotalAmount(managerId)
        );
        return "manager/booking";
    }

    // ================= HELPER =================
    private Long getManagerId(Authentication authentication) {
        String username = authentication.getName();
        if (username.matches("\\d+")) {
            return Long.parseLong(username);
        }

        User manager = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Logged in manager not found"));
        return manager.getId();
    }
}
