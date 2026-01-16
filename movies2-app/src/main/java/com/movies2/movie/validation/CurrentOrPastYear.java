package com.movies2.movie.validation;

import jakarta.validation.*;
import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrentOrPastYearValidator.class)
public @interface CurrentOrPastYear {
    String message() default "Year must not be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
