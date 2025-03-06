package com.example.stayfinder.service.payment;

import com.example.stayfinder.dto.payment.PaymentDto;
import com.example.stayfinder.dto.payment.PaymentLowInfoDto;
import com.example.stayfinder.dto.payment.PaymentWithoutSessionDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    List<PaymentLowInfoDto> findAllByBookingUserId(Long userId, Pageable pageable);

    PaymentDto createSession(Long bookingId);

    PaymentWithoutSessionDto processSuccessfulPayment(String sessionId);

    String processCancelPayment(String sessionId);

    boolean existsByBookingUserIdAndStatus(Long userId);
}
