package com.movies2.movie.dto;

public record MovieListRequestDto(
        String title,
        Long directorId,
        String image,
        Integer yearFrom,
        Integer yearTo,
        String genreContains,
        Integer page,
        Integer size
) { }
