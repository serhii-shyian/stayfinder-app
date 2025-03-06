package com.example.stayfinder.dto.accommodation;

import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.validation.EnumValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Set;

public record AccommodationRequestDto(
        @NotBlank
        @Pattern(regexp = "HOUSE|APARTMENT|CONDO|VACATION_HOME",
                message = "Use one of types: HOUSE, APARTMENT, CONDO, VACATION_HOME")
        String type,
        @NotBlank(message = "Accommodation location is required")
        String location,
        @NotBlank(message = "Accommodation size is required")
        String size,
        @NotBlank
        @EnumValidator(enumClass = Accommodation.Amenities.class,
        message = "Invalid amenities value")
        Set<String> amenities,
        @Positive
        BigDecimal dailyRate,
        @Positive
        Integer availability){
}
