package com.movies2.director;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectorRepository extends JpaRepository<Director, Long> {
    boolean existsByNameIgnoreCase(String name);
}
