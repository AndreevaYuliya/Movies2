package com.movies2.movie.dto;

import org.antlr.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.*;

public record MovieRequestDto(
        @NotBlank String title,
        @Min(1880) @Max(2100) Integer yearReleased,
        String genres,
        Double rating,
        @NotNull Long directorId
) { }
