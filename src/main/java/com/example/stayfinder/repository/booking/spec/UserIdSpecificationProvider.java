package com.example.stayfinder.repository.booking.spec;

import com.example.stayfinder.model.Booking;
import com.example.stayfinder.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserIdSpecificationProvider implements SpecificationProvider<Booking> {
    private static final String KEY = "userId";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public Specification<Booking> getSpecification(String[] params) {
        return (root, query, criteriaBuilder)
                -> root.get("user").get("id").in(
                Arrays.stream(params).map(Long::valueOf).toList()
        );
    }
}
