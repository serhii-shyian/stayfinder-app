package com.example.stayfinder.dto.booking;

import com.example.stayfinder.validation.CheckInCheckOut;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@CheckInCheckOut
public record CreateBookingRequestDto(
        @NotNull(message = "Check in date is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime checkInDate,
        @NotNull(message = "Check out date is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime checkOutDate,
        @NotNull(message = "Accommodation id is required")
        @Positive
        Long accommodationId) {
}
