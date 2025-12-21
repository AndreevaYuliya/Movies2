package com.movies2.director;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, Long> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Director> findByNameIgnoreCase(String name);
}
