package com.example.stayfinder.dto.booking;

import java.time.LocalDateTime;

public record BookingDto(
        Long id,
        LocalDateTime checkInDate,
        LocalDateTime checkOutDate,
        Long accommodationId,
        String userName,
        String status) {
}
