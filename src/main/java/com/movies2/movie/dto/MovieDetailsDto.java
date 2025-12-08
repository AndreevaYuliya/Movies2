package com.movies2.movie.dto;

public record MovieDetailsDto(
        Long id,
        String title,
        Integer yearReleased,
        String genres,
        Double rating,
        DirectorInfo director
) {
    public record DirectorInfo(Long id, String name, String country) {}
}
