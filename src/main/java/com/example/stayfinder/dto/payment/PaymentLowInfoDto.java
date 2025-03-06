package com.example.stayfinder.dto.payment;

import java.math.BigDecimal;

public record PaymentLowInfoDto(
        Long id,
        String sessionId,
        BigDecimal amount,
        String status) {
}
