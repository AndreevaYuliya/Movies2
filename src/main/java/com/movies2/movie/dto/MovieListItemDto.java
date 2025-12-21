package com.movies2.movie.dto;

public record MovieListItemDto(Long id,
                               String title,
                               String image,
                               Integer yearReleased,
                               String directorName
) { }
