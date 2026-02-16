package com.hotelbooking.controller;

import com.hotelbooking.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin", description = "Administrative dashboard and management endpoints")
public class AdminController {

    private final AdminService adminService;

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    @Operation(summary = "View admin dashboard", description = "Loads metrics such as users, hotels, bookings and revenue.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard page loaded")
    })
    public String dashboard(Model model) {

        model.addAttribute("totalUsers", adminService.getAllUsers().size());
        model.addAttribute("totalHotels", adminService.getAllHotels().size());
        model.addAttribute("totalBookings", adminService.getAllBookings().size());
        model.addAttribute("totalRevenue", adminService.getTotalRevenue());

        return "admin/dashboard";
    }

    // ================= USERS =================
    @GetMapping("/users")
    @Operation(summary = "View users", description = "Displays all users for administrative review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users page loaded")
    })
    public String viewUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    // ================= HOTEL MANAGERS =================
    @PostMapping("/managers/{userId}/approve")
    @Operation(summary = "Approve hotel manager", description = "Approves a pending hotel manager account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Manager approved and redirected"),
            @ApiResponse(responseCode = "404", description = "Manager not found", content = @Content(schema = @Schema()))
    })
    public String approveHotelManager(
            @Parameter(description = "Manager user id", example = "10") @PathVariable Long userId,
            RedirectAttributes ra
    ) {
        adminService.approveHotelManager(userId);
        ra.addFlashAttribute("success", "Hotel manager approved successfully");
        return "redirect:/admin/users";
    }

    @PostMapping("/managers/{userId}/delete")
    @Operation(summary = "Delete hotel manager", description = "Deletes an existing hotel manager account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Manager deleted and redirected"),
            @ApiResponse(responseCode = "404", description = "Manager not found", content = @Content(schema = @Schema()))
    })
    public String deleteHotelManager(
            @Parameter(description = "Manager user id", example = "10") @PathVariable Long userId,
            RedirectAttributes ra
    ) {
        adminService.deleteHotelManager(userId);
        ra.addFlashAttribute("success", "Hotel manager deleted successfully");
        return "redirect:/admin/users";
    }

    // ================= HOTELS =================
    @GetMapping("/hotels")
    @Operation(summary = "View hotels", description = "Displays all hotels in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hotels page loaded")
    })
    public String viewHotels(Model model) {
        model.addAttribute("hotels", adminService.getAllHotels());
        return "admin/hotels";
    }

    // ================= HOTEL DETAILS =================
    @GetMapping("/hotels/{hotelId}")
    @Operation(summary = "View hotel details", description = "Displays hotel and room details for a specific hotel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hotel details page loaded"),
            @ApiResponse(responseCode = "404", description = "Hotel not found", content = @Content(schema = @Schema()))
    })
    public String hotelDetails(
            @Parameter(description = "Hotel id", example = "101") @PathVariable Long hotelId,
            Model model
    ) {
        model.addAttribute("hotel", adminService.getHotelDetails(hotelId));
        model.addAttribute("rooms", adminService.getHotelRooms(hotelId));
        return "admin/hotelDetails";
    }

    // ================= BOOKINGS =================
    @GetMapping("/bookings")
    @Operation(summary = "View bookings", description = "Displays all bookings in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings page loaded")
    })
    public String viewBookings(Model model) {
        model.addAttribute("bookings", adminService.getAllBookings());
        return "admin/booking";
    }
}
