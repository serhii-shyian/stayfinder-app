package com.example.stayfinder.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePaymentSessionDto(
        @NotNull(message = "Booking id is required")
        @Positive
        Long bookingId) {
}
