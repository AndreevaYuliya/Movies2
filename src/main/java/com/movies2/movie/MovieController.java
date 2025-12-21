package com.movies2.movie;

import com.movies2.movie.dto.MovieDetailsDto;
import com.movies2.movie.dto.MovieListRequestDto;
import com.movies2.movie.dto.MovieListResponseDto;
import com.movies2.movie.dto.MovieRequestDto;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/api/movies")
class MovieController {
    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieDetailsDto create(@Valid @RequestBody MovieRequestDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public MovieDetailsDto get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public MovieDetailsDto update(@PathVariable Long id, @Valid @RequestBody MovieRequestDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/_list")
    public MovieListResponseDto list(@RequestBody MovieListRequestDto filter) {
        return service.list(filter);
    }

    @PostMapping("/_report")
    public ResponseEntity<byte[]> report(@RequestBody MovieListRequestDto filter) {
        byte[] csv = service.generateReport(filter);

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType("text/csv"));
        h.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"movies_report.csv\"");

        return new ResponseEntity<>(csv, h, HttpStatus.OK);
    }

    @PostMapping("/upload")
    public MovieService.UploadResult upload(@RequestParam("file") MultipartFile file) throws IOException {
        return service.upload(file);
    }
}
