package com.example.stayfinder.repository.booking.spec;

import com.example.stayfinder.model.Booking;
import com.example.stayfinder.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class StatusSpecificationProvider implements SpecificationProvider<Booking> {
    private static final String KEY = "status";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public Specification<Booking> getSpecification(String[] params) {
        return (root, query, criteriaBuilder)
                -> root.get(KEY).in(Arrays.stream(params).toArray());
    }
}
