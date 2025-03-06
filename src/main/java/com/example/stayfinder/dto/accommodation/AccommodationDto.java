package com.example.stayfinder.dto.accommodation;

import java.math.BigDecimal;
import java.util.Set;

public record AccommodationDto(
        Long id,
        String type,
        String location,
        String size,
        Set<String> amenities,
        BigDecimal dailyRate,
        Integer availability) {
}
