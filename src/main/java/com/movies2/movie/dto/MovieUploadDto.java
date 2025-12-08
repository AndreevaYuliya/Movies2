package com.movies2.movie.dto;

public record MovieUploadDto(
        String title,
        String director,
        int year,
        String genres
) { }
