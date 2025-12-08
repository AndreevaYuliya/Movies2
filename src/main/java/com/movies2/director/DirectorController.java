package com.movies2.director;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directors")
class DirectorController {
    private final DirectorService service;

    public DirectorController(DirectorService service) {
        this.service = service;
    }

    @GetMapping
    public List<DirectorDto> getAll() {
        return service.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto create(@Valid @RequestBody DirectorDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public DirectorDto update(@PathVariable Long id, @Valid @RequestBody DirectorDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
