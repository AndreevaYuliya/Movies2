package com.movies2.movie.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Year;

public class CurrentOrPastYearValidator implements ConstraintValidator<CurrentOrPastYear, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext ctx) {
        if (value == null) {
            return true;
        }          // добавь @NotNull, если нужно требовать
        int now = Year.now().getValue();
        return value >= 1880 && value <= now;
    }
}
