package com.hotelbooking.mapper;

import com.hotelbooking.dto.PaymentDTO;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentDTO dto, Booking booking) {
        return Payment.builder()
                .id(dto.getP_id())
                .booking(booking)
                .amount(dto.getAmount())
                .paymentStatus(dto.getPaymentStatus())
                .paymentReferenceId(dto.getPaymentReferenceId())
                .paymentProvider(dto.getPaymentProvider())
                .build();
    }

    public PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
                .p_id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .paymentReferenceId(payment.getPaymentReferenceId())
                .paymentProvider(payment.getPaymentProvider())
                .build();
    }
}
