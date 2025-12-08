package com.movies2.movie.dto;

public record MovieListRequestDto(
        Long directorId,
        Integer yearFrom,
        Integer yearTo,
        String genreContains,
        Integer page,
        Integer size
) { }
