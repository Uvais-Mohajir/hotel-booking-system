package com.hotelbooking.dto;

import com.hotelbooking.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "BookingDTO", description = "Booking request/response payload")
public class BookingDTO {

    @Schema(description = "Booking id", example = "5001")
    private Long b_id;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Check-in date and time", example = "2026-03-10T12:00:00")
    private LocalDateTime checkIn;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Check-out date and time", example = "2026-03-12T11:00:00")
    private LocalDateTime checkOut;

    @Schema(description = "Booking status", example = "CONFIRMED")
    private BookingStatus status;

    @Schema(description = "Total booking amount", example = "4999.99")
    private double tAmount;

    @NotNull
    @Schema(description = "Customer user id", example = "100")
    private Long userId;

    @NotNull
    @Schema(description = "Hotel id", example = "101")
    private Long hotelId;

    @NotNull
    @Schema(description = "Room id", example = "201")
    private Long roomId;
}
