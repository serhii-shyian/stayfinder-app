package com.example.stayfinder.dto.payment;

import java.math.BigDecimal;

public record PaymentWithoutSessionDto(
        Long bookingId,
        String status,
        BigDecimal amount) {
}
