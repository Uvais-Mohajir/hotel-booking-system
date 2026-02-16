package com.hotelbooking.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.repository.HotelRepository;
import org.springframework.ui.Model;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Tag(name = "Home", description = "Public home and logout endpoints")
public class HomeController {

    private final HotelRepository hotelRepository;

    @GetMapping({"/", "/index"})
    @Operation(summary = "Home page", description = "Loads landing page with latest hotels.")
    @ApiResponse(responseCode = "200", description = "Home page loaded")
    public String index(Model model) {

        List<Hotel> hotels = hotelRepository.findTop4ByOrderByIdDesc();

        model.addAttribute("hotels", hotels);
        return "index";
    }

    // ================= COMMON LOGOUT =================
    @GetMapping("/logout")
    @Operation(summary = "Logout", description = "Logs out currently authenticated user and redirects to home page.")
    @ApiResponse(responseCode = "302", description = "Logged out and redirected")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler()
                    .logout(request, response, auth);
        }

        // Redirect to landing page
        return "redirect:/index";
    }
}
