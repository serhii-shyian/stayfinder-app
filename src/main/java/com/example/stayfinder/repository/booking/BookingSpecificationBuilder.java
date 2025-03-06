package com.example.stayfinder.repository.booking;

import com.example.stayfinder.dto.booking.BookingFilterParameters;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.repository.SpecificationBuilder;
import com.example.stayfinder.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingSpecificationBuilder implements SpecificationBuilder<Booking> {
    private final SpecificationProviderManager<Booking> specificationProviderManager;

    @Override
    public Specification<Booking> build(BookingFilterParameters parameters) {
        Specification<Booking> spec = Specification.where(null);
        if (parameters.userIdArray() != null && parameters.userIdArray().length > 0) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("userId")
                    .getSpecification(parameters.userIdArray()));
        }
        if (parameters.statusArray() != null && parameters.statusArray().length > 0) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("status")
                    .getSpecification(parameters.statusArray()));
        }
        return spec;
    }
}
