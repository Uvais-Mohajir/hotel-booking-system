package com.hotelbooking.service;

import com.hotelbooking.dto.PaymentDTO;

public interface PaymentService {

    PaymentDTO createRazorpayOrder(Long bookingId, Long userId);

    void verifyAndSavePayment(
            Long bookingId,
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature,
            Long userId
    );
}
