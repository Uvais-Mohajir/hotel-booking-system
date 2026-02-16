package com.hotelbooking.service.impl;

import com.hotelbooking.dto.*;
import com.hotelbooking.entity.*;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.enums.*;
import com.hotelbooking.mapper.*;
import com.hotelbooking.repository.*;
import com.hotelbooking.service.*;
import com.razorpay.*;
import lombok.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    // ================= CREATE RAZORPAY ORDER =================
    @Override
    public PaymentDTO createRazorpayOrder(Long bookingId, Long userId) {

        Booking booking = getAuthorizedBooking(bookingId, userId);

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", booking.getTAmount() * 100); // INR paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + bookingId);

            Order order = razorpayClient.orders.create(orderRequest);

            Payment payment = paymentRepository.findByBooking_Id(bookingId)
                    .orElse(
                            Payment.builder()
                                    .booking(booking)
                                    .amount(booking.getTAmount())
                                    .paymentProvider("RAZORPAY")
                                    .build()
                    );

            payment.setAmount(booking.getTAmount());
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setPaymentReferenceId(order.get("id"));

            paymentRepository.save(payment);

            return paymentMapper.toDTO(payment);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }

    // ================= VERIFY PAYMENT =================
    @Override
    public void verifyAndSavePayment(
            Long bookingId,
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature,
            Long userId
    ) {

        Booking booking = getAuthorizedBooking(bookingId, userId);

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(
                    options,
                    razorpayKeySecret
            );

            if (!isValid) {
                throw new RuntimeException("Payment verification failed");
            }

            Payment payment = paymentRepository.findByBooking_Id(bookingId)
                    .orElseThrow(() -> new RuntimeException("Payment record not found"));

            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPaymentReferenceId(razorpayOrderId + "|" + razorpayPaymentId);
            paymentRepository.save(payment);

            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

        } catch (Exception e) {
            throw new RuntimeException("Payment verification failed", e);
        }
    }

    // ================= COMMON AUTH CHECK =================
    private Booking getAuthorizedBooking(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized payment access");
        }

        return booking;
    }
}
