package com.example.stayfinder.repository;

import com.example.stayfinder.dto.booking.BookingFilterParameters;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    Specification<T> build(BookingFilterParameters parameters);
}
