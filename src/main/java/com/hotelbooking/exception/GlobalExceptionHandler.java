package com.hotelbooking.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /* ================= AUTH ================= */

    @ExceptionHandler(AuthenticationFailedException.class)
    public String handleAuthError(
            AuthenticationFailedException ex,
            Model model) {

        model.addAttribute("error", ex.getMessage());
        return "login-error"; // optional generic page
    }

    /* ================= RESOURCE ================= */

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(
            ResourceNotFoundException ex,
            Model model) {

        model.addAttribute("error", ex.getMessage());
        return "error/404";
    }

    /* ================= BUSINESS ================= */

    @ExceptionHandler(InvalidOperationException.class)
    public String handleInvalidOperation(
            InvalidOperationException ex,
            Model model) {

        model.addAttribute("error", ex.getMessage());
        return "error/business-error";
    }

    /* ================= FALLBACK ================= */

    @ExceptionHandler(Exception.class)
    public String handleGeneric(
            Exception ex,
            Model model) {

        model.addAttribute("error", "Something went wrong. Please try again.");
        return "error/500";
    }
}
