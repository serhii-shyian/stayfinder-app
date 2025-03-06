package com.example.stayfinder.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CheckInCheckOutValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckInCheckOut {
    String message() default "The check-in date must be before the check-out date.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
