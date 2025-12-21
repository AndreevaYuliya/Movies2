package com.movies2.movie.dto;

public record MovieDetailsDto(
        Long id,
        String title,
        String image,
        String description,
        Integer yearReleased,
        String genres,
        Double rating,
        DirectorInfo director
) {
    public record DirectorInfo(Long id, String name, String country) {}
}
