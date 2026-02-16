package com.hotelbooking.controller;

import com.hotelbooking.dto.BookingDTO;
import com.hotelbooking.dto.PaymentDTO;
import com.hotelbooking.dto.RoomDTO;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.User;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.service.CustomerService;
import com.hotelbooking.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
@Tag(name = "Customer", description = "Customer booking and payment endpoints")
public class CustomerController {

    private final CustomerService customerService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    // ================= USER DASHBOARD =================
    @GetMapping("/dashboard")
    @Operation(summary = "View customer dashboard", description = "Loads featured hotels for the logged-in customer.")
    @ApiResponse(responseCode = "200", description = "Dashboard page loaded")
    public String dashboard(Model model) {
        model.addAttribute("hotels", customerService.getFeaturedHotels());
        return "user/dashboard";
    }

    // ================= SEARCH HOTELS =================
    @GetMapping("/hotels/search")
    @Operation(summary = "Search hotels", description = "Searches hotels by city. Returns featured hotels when city is blank.")
    @ApiResponse(responseCode = "200", description = "Search page loaded")
    public String searchHotels(
            @Parameter(description = "City name to search", example = "Hyderabad") @RequestParam(required = false) String city,
            Model model
    ) {
        if (city != null && !city.isBlank()) {
            List<Hotel> hotels = customerService.searchHotels(city);
            model.addAttribute("hotels", hotels);
        } else {
            model.addAttribute("hotels", customerService.getFeaturedHotels());
        }
        return "user/searchHotelsViewHotels";
    }

    @GetMapping("/search-room")
    @Operation(summary = "Search room page", description = "Returns room search page.")
    @ApiResponse(responseCode = "200", description = "Search room page loaded")
    public String searchRoomPage() {
        return "user/searchRoom";
    }

    // ================= VIEW HOTEL DETAILS =================
    @GetMapping("/hotels/{hotelId}")
    @Operation(summary = "View hotel details", description = "Shows selected hotel and available rooms.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hotel details loaded"),
            @ApiResponse(responseCode = "404", description = "Hotel not found", content = @Content(schema = @Schema()))
    })
    public String viewHotelDetails(
            @Parameter(description = "Hotel id", example = "101") @PathVariable Long hotelId,
            Model model
    ) {
        Hotel hotel = customerService.viewHotelDetails(hotelId);
        List<RoomDTO> rooms =
                customerService.viewAvailableRooms(hotelId);

        model.addAttribute("hotel", hotel);
        model.addAttribute("rooms", rooms);

        return "user/bookRoom";
    }

    // ================= BOOK ROOM =================
    @PostMapping("/book")
    @Operation(summary = "Book room", description = "Creates a reservation for the logged-in customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Booking created and redirected to payment"),
            @ApiResponse(responseCode = "400", description = "Invalid booking request", content = @Content(schema = @Schema()))
    })
    public String bookRoom(
            @Parameter(description = "Booking request payload") @ModelAttribute BookingDTO bookingDTO,
            @Parameter(hidden = true) Authentication authentication,
            RedirectAttributes ra
    ) {
        Long userId = getUserId(authentication);

        Long bookingId;
        try {
            bookingId = customerService.bookRoom(bookingDTO, userId);
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/user/hotels/" + bookingDTO.getHotelId();
        }

        ra.addFlashAttribute("success", "Room reserved. Complete payment to confirm.");
        return "redirect:/user/bookings/" + bookingId + "/payment";
    }

    @GetMapping("/bookings/{bookingId}/payment")
    @Operation(summary = "Open payment page", description = "Loads booking payment information and creates a Razorpay order when needed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment page loaded"),
            @ApiResponse(responseCode = "302", description = "Redirected on payment initialization failure")
    })
    public String paymentPage(
            @Parameter(description = "Booking id", example = "5001") @PathVariable Long bookingId,
            @Parameter(hidden = true) Authentication authentication,
            Model model,
            RedirectAttributes ra
    ) {
        Long userId = getUserId(authentication);

        BookingDTO booking;
        PaymentDTO payment;
        try {
            booking = customerService.viewBooking(bookingId, userId);
            payment = customerService.viewPaymentDetails(bookingId, userId);
            if (payment == null || payment.getPaymentStatus() == null
                    || payment.getPaymentStatus().name().equals("FAILED")) {
                payment = paymentService.createRazorpayOrder(bookingId, userId);
            }
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", "Unable to create payment order. Please try again.");
            return "redirect:/user/bookings/" + bookingId;
        }

        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("razorpayKeyId", razorpayKeyId);

        return "user/payment";
    }

    @PostMapping("/bookings/{bookingId}/payment/verify")
    @Operation(summary = "Verify payment", description = "Verifies Razorpay payment signature and stores payment details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Payment verified and redirected"),
            @ApiResponse(responseCode = "400", description = "Payment verification failed", content = @Content(schema = @Schema()))
    })
    public String verifyPayment(
            @Parameter(description = "Booking id", example = "5001") @PathVariable Long bookingId,
            @Parameter(description = "Razorpay payment id") @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @Parameter(description = "Razorpay order id") @RequestParam("razorpay_order_id") String razorpayOrderId,
            @Parameter(description = "Razorpay signature") @RequestParam("razorpay_signature") String razorpaySignature,
            @Parameter(hidden = true) Authentication authentication,
            RedirectAttributes ra
    ) {
        Long userId = getUserId(authentication);

        paymentService.verifyAndSavePayment(
                bookingId,
                razorpayPaymentId,
                razorpayOrderId,
                razorpaySignature,
                userId
        );

        ra.addFlashAttribute("success", "Payment successful");
        return "redirect:/user/bookings/" + bookingId;
    }

    // ================= VIEW SINGLE BOOKING =================
    @GetMapping("/bookings/{bookingId}")
    @Operation(summary = "View booking details", description = "Shows booking and payment details for a specific booking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking details loaded"),
            @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content(schema = @Schema()))
    })
    public String viewBooking(
            @Parameter(description = "Booking id", example = "5001") @PathVariable Long bookingId,
            @Parameter(hidden = true) Authentication authentication,
            Model model
    ) {
        Long userId = getUserId(authentication);

        BookingDTO booking =
                customerService.viewBooking(bookingId, userId);
        PaymentDTO payment =
                customerService.viewPaymentDetails(bookingId, userId);

        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);

        return "user/bookingDetails";
    }

    // ================= VIEW MY BOOKINGS =================
    @GetMapping("/bookings")
    @Operation(summary = "View my bookings", description = "Lists all bookings for the logged-in customer.")
    @ApiResponse(responseCode = "200", description = "Bookings page loaded")
    public String myBookings(
            @Parameter(hidden = true) Authentication authentication,
            Model model
    ) {
        Long userId = getUserId(authentication);

        List<BookingDTO> bookings =
                customerService.viewMyBookings(userId);

        model.addAttribute("bookings", bookings);
        return "user/booking";
    }

    // ================= CANCEL BOOKING =================
    @PostMapping("/bookings/{bookingId}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancels a customer booking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Booking cancelled and redirected"),
            @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content(schema = @Schema()))
    })
    public String cancelBooking(
            @Parameter(description = "Booking id", example = "5001") @PathVariable Long bookingId,
            @Parameter(hidden = true) Authentication authentication,
            RedirectAttributes ra
    ) {
        Long userId = getUserId(authentication);

        customerService.cancelBooking(bookingId, userId);
        ra.addFlashAttribute("success", "Booking cancelled");

        return "redirect:/user/bookings";
    }

    // ================= HELPER =================
    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        if (username.matches("\\d+")) {
            return Long.parseLong(username);
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
        return user.getId();
    }
}
