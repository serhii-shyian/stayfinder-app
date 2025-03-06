package com.example.stayfinder.validation;

import com.example.stayfinder.dto.booking.CreateBookingRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class CheckInCheckOutValidator implements ConstraintValidator<CheckInCheckOut,
        CreateBookingRequestDto> {

    @Override
    public boolean isValid(CreateBookingRequestDto createBookingRequestDto,
                           ConstraintValidatorContext context) {
        LocalDateTime checkInDate = createBookingRequestDto.checkInDate();
        LocalDateTime checkOutDate = createBookingRequestDto.checkOutDate();
        LocalDateTime now = LocalDateTime.now();

        return !checkInDate.isBefore(now) && !checkInDate.isAfter(checkOutDate);
    }
}
