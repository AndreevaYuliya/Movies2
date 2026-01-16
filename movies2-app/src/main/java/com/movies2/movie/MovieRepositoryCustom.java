package com.movies2.movie;

import com.movies2.movie.dto.MovieListRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieRepositoryCustom {
    Page<MovieEntity> findByFilter(MovieListRequestDto filter, Pageable pageable);
}
