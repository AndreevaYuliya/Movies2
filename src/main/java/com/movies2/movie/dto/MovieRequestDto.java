package com.movies2.movie.dto;

import com.movies2.movie.validation.CurrentOrPastYear;
import jakarta.validation.constraints.*;

public record MovieRequestDto(
        @NotBlank String title,
        String image,
        String description,
        @CurrentOrPastYear Integer yearReleased,
        String genres,
        @DecimalMin("0.0")
        @DecimalMax("10.0")
        Double rating,
        @NotNull Long directorId
) { }
