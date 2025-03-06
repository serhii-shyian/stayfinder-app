package com.example.stayfinder.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumValidatorConstraint implements ConstraintValidator<EnumValidator, Set<String>> {
    private Set<String> validValues;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        validValues = Stream.of(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Set<String> values, ConstraintValidatorContext context) {
        return validValues.containsAll(values);
    }
}
