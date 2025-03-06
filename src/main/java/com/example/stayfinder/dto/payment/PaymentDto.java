package com.example.stayfinder.dto.payment;

import java.math.BigDecimal;

public record PaymentDto(
        Long id,
        Long bookingId,
        String sessionId,
        String sessionUrl,
        BigDecimal amount,
        String status) {
}
