package com.movies2.director;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional

public class DirectorService {

    private final DirectorRepository repo;

    public DirectorService(DirectorRepository repo) {
        this.repo = repo;
    }

    public List<DirectorDto> getAll() {
        return repo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public DirectorDto create(DirectorDto dto) {
        if (repo.existsByNameIgnoreCase(dto.name()))
            throw new IllegalArgumentException("Director already exists");

        Director d = new Director();
        d.setName(dto.name());
        d.setCountry(dto.country());
        return toDto(repo.save(d));
    }

    public DirectorDto update(Long id, DirectorDto dto) {
        Director d = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Director not found"));

        if (!d.getName().equalsIgnoreCase(dto.name())
                && repo.existsByNameIgnoreCase(dto.name())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Director already exists"
            );
        }

        d.setName(dto.name());
        d.setCountry(dto.country());
        return toDto(d);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Director getByIdOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Director not found"));
    }

    public Director getByNameOrThrow(String name) {
        return repo.findByNameIgnoreCase(name)
                .orElseThrow(() ->
                        new EntityNotFoundException("Director not found: " + name)
                );
    }

    private DirectorDto toDto(Director d) {
        return new DirectorDto(d.getId(), d.getName(), d.getCountry());
    }
}