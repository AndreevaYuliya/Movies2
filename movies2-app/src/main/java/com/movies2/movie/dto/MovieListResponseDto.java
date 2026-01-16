package com.movies2.movie.dto;

import java.util.List;

public record MovieListResponseDto(
        List<MovieListItemDto> list,
        int totalPages
) { }
