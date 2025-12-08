package com.movies2.director;

import jakarta.validation.constraints.NotBlank;

public record DirectorDto(Long id,
                          @NotBlank(message = "Name is required")
                          String name,
                          String country
) { }
