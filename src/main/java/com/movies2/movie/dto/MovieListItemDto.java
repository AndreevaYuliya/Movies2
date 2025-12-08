package com.movies2.movie.dto;

public record MovieListItemDto(Long id,
                               String title,
                               Integer year,
                               String directorName
) { }
