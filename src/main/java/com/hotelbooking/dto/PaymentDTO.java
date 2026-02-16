package com.hotelbooking.dto;

import com.hotelbooking.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PaymentDTO", description = "Payment details payload")
public class PaymentDTO {

    @Schema(description = "Payment id", example = "9001")
    private Long p_id;

    @NotNull
    @Schema(description = "Booking id associated with payment", example = "5001")
    private Long bookingId;

    @Positive
    @Schema(description = "Payment amount", example = "4999.99")
    private double amount;

    @Schema(description = "Payment status", example = "SUCCESS")
    private PaymentStatus paymentStatus;

    @Schema(description = "Provider reference transaction id", example = "pay_Q2Y3abc123")
    private String paymentReferenceId;

    @Schema(description = "Payment provider name", example = "RAZORPAY")
    private String paymentProvider;
}
