package com.example.stayfinder.dto.accommodation;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AccommodationDto {
    private Long id;
    private String type;
    private String location;
    private String size;
    private Set<String> amenities;
    private BigDecimal dailyRate;
    private Integer availability;
}
