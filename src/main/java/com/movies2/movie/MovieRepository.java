package com.movies2.movie;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<MovieEntity, Long>, MovieRepositoryCustom{
}
