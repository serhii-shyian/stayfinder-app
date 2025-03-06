package com.example.stayfinder.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<
        Password, Object> {
    private static final String PASSWORD_PATTERN = ".{8,35}";

    @Override
    public boolean isValid(Object value,
                           ConstraintValidatorContext validatorContext) {
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher((CharSequence) value);
        return matcher.matches();
    }
}
