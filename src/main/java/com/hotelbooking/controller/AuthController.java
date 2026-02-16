package com.hotelbooking.controller;

import com.hotelbooking.dto.*;
import com.hotelbooking.enums.Role;
import com.hotelbooking.exception.AuthenticationFailedException;
import com.hotelbooking.mapper.ManagerHotelMapper;
import com.hotelbooking.mapper.UserMapper;
import com.hotelbooking.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final ManagerHotelMapper managerHotelMapper;

    @GetMapping("/adminLogin")
    @Operation(summary = "Admin login page", description = "Returns admin login form.")
    @ApiResponse(responseCode = "200", description = "Admin login page loaded")
    public String adminLoginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "AdminLogin";
    }

    @PostMapping("/adminLogin")
    @Operation(summary = "Admin login", description = "Authenticates an admin and creates session security context.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Login successful and redirected to admin dashboard"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema()))
    })
    public String adminLogin(
            @Parameter(description = "Admin login payload") @ModelAttribute @Valid LoginDTO loginDTO,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            authService.login(
                    loginDTO.getEmail(),
                    loginDTO.getPassword(),
                    Role.ROLE_ADMIN
            );
            saveSecurityContextToSession(request);
            return "redirect:/admin/dashboard";
        } catch (AuthenticationFailedException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/adminLogin";
        }
    }

    @GetMapping("/userRegister")
    @Operation(summary = "User registration page", description = "Returns user registration form.")
    @ApiResponse(responseCode = "200", description = "User registration page loaded")
    public String userRegisterPage(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "UserRegister";
    }

    @PostMapping("/userRegister")
    @Operation(summary = "Register user", description = "Registers a new customer account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Registration successful and redirected to user login"),
            @ApiResponse(responseCode = "400", description = "Invalid user details", content = @Content(schema = @Schema()))
    })
    public String userRegister(
            @Parameter(description = "User registration payload") @ModelAttribute @Valid UserDTO userDTO,
            RedirectAttributes redirectAttributes
    ) {
        userDTO.setRole(Role.ROLE_USER);
        userDTO.setEnabled(true);

        authService.registerUser(
                userMapper.toEntity(userDTO),
                Role.ROLE_USER
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Registration successful. Please login."
        );
        return "redirect:/userLogin";
    }

    @GetMapping("/userLogin")
    @Operation(summary = "User login page", description = "Returns user login form.")
    @ApiResponse(responseCode = "200", description = "User login page loaded")
    public String userLoginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "UserLogin";
    }

    @PostMapping("/userLogin")
    @Operation(summary = "User login", description = "Authenticates a customer and creates session security context.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Login successful and redirected to user dashboard"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema()))
    })
    public String userLogin(
            @Parameter(description = "User login payload") @ModelAttribute @Valid LoginDTO loginDTO,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            authService.login(
                    loginDTO.getEmail(),
                    loginDTO.getPassword(),
                    Role.ROLE_USER
            );
            saveSecurityContextToSession(request);
            return "redirect:/user/dashboard";
        } catch (AuthenticationFailedException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/userLogin";
        }
    }

    @GetMapping("/managerRegister")
    @Operation(summary = "Manager registration page", description = "Returns hotel manager registration form.")
    @ApiResponse(responseCode = "200", description = "Manager registration page loaded")
    public String managerRegisterPage(Model model) {
        model.addAttribute("managerDTO", new ManagerDTO());
        return "ManagerRegister";
    }

    @PostMapping("/managerRegister")
    @Operation(summary = "Register manager", description = "Registers a hotel manager account pending admin approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Registration successful and redirected to manager login"),
            @ApiResponse(responseCode = "400", description = "Invalid manager details", content = @Content(schema = @Schema()))
    })
    public String managerRegister(
            @Parameter(description = "Manager registration payload") @ModelAttribute @Valid ManagerDTO managerDTO,
            RedirectAttributes redirectAttributes
    ) {
        managerDTO.setRole(Role.ROLE_HOTEL_MANAGER);
        managerDTO.setEnabled(false); // admin approval required

        authService.registerHotelManager(
                managerHotelMapper.toUserEntity(managerDTO),
                managerHotelMapper.toHotelEntity(managerDTO),
                Role.ROLE_HOTEL_MANAGER
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Registration successful. Wait for admin approval."
        );
        return "redirect:/managerLogin";
    }

    @GetMapping("/managerLogin")
    @Operation(summary = "Manager login page", description = "Returns manager login form.")
    @ApiResponse(responseCode = "200", description = "Manager login page loaded")
    public String managerLoginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "ManagerLogin";
    }

    @PostMapping("/managerLogin")
    @Operation(summary = "Manager login", description = "Authenticates a hotel manager and creates session security context.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Login successful and redirected to manager dashboard"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema()))
    })
    public String managerLogin(
            @Parameter(description = "Manager login payload") @ModelAttribute @Valid LoginDTO loginDTO,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            authService.login(
                    loginDTO.getEmail(),
                    loginDTO.getPassword(),
                    Role.ROLE_HOTEL_MANAGER
            );
            saveSecurityContextToSession(request);
            return "redirect:/manager/dashboard";
        } catch (AuthenticationFailedException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/managerLogin";
        }
    }

    private void saveSecurityContextToSession(HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
    }
}
