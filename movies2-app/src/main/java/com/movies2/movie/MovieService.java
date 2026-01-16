package com.movies2.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movies2.director.Director;
import com.movies2.director.DirectorRepository;
import com.movies2.director.DirectorService;

import com.movies2.movie.dto.*;
import com.movies2.service.EmailProducer;
import jakarta.persistence.EntityNotFoundException;

import com.movies2.movie.dto.*;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional
public class MovieService {

    private final EmailProducer emailProducer;


    private final MovieRepository movieRepo;
    private final DirectorRepository directorRepo;
    private final DirectorService directorService;
    private final ObjectMapper objectMapper;

    private void applyDto(MovieEntity movie, MovieRequestDto dto, Director director) {
        movie.setTitle(dto.title());
        movie.setImage(dto.image());
        movie.setDescription(dto.description());
        movie.setYearReleased(dto.yearReleased());
        movie.setGenres(dto.genres());
        movie.setRating(dto.rating());
        movie.setDirector(director);
    }

    public MovieService(
            MovieRepository movieRepo,
            DirectorRepository directorRepo,
            DirectorService directorService,
            ObjectMapper objectMapper,
            EmailProducer emailProducer) {
        this.movieRepo = movieRepo;
        this.directorRepo = directorRepo;
        this.directorService = directorService;
        this.objectMapper = objectMapper;
        this.emailProducer = emailProducer;
    }

    // ----------------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------------
    public MovieDetailsDto create(MovieRequestDto dto) {
        Director director = directorService.getByIdOrThrow(dto.directorId());

        MovieEntity movie = new MovieEntity();
        applyDto(movie, dto, director);

        movieRepo.save(movie);

        emailProducer.sendCreatedNotification("admin@movies.com");

        return toDetailsDto(movie);
    }

    // ----------------------------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------------------------
    public MovieDetailsDto getById(Long id) {
        MovieEntity movie = movieRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found"));
        return toDetailsDto(movie);
    }

    // ----------------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------------
    public MovieDetailsDto update(Long id, MovieRequestDto dto) {
        MovieEntity movie = movieRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found"));

        Director director = directorService.getByIdOrThrow(dto.directorId());

        applyDto(movie, dto, director);

        return toDetailsDto(movie);
    }

    // ----------------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------------
    public void delete(Long id) {
        if (!movieRepo.existsById(id)) {
            throw new EntityNotFoundException("Movie not found");
        }
        movieRepo.deleteById(id);
        movieRepo.flush();
    }

    // ----------------------------------------------------------------------
    // LIST + FILTER + PAGINATION
    // ----------------------------------------------------------------------
    public MovieListResponseDto list(MovieListRequestDto filter) {
        int page = filter.page() != null ? filter.page() : 0;
        int size = filter.size() != null ? filter.size() : 20;

        Pageable pageable = PageRequest.of(page, size);

        Page<MovieEntity> result = movieRepo.findByFilter(filter, pageable);

        List<MovieListItemDto> list = result.getContent().stream()
                .map(m -> new MovieListItemDto(
                        m.getId(),
                        m.getTitle(),
                        m.getImage(),
                        m.getYearReleased(),
                        m.getDirector().getName()
                ))
                .toList();

        return new MovieListResponseDto(list, result.getTotalPages());
    }

    // ----------------------------------------------------------------------
    // CSV REPORT
    // ----------------------------------------------------------------------
    public byte[] generateReport(MovieListRequestDto filter) {
        Page<MovieEntity> result = movieRepo.findByFilter(filter, Pageable.unpaged());

        StringBuilder sb = new StringBuilder();
        sb.append("id,title,year,genres,rating,director\n");

        for (MovieEntity m : result.getContent()) {
            sb.append(m.getId()).append(",");
            sb.append(escapeCsv(m.getTitle())).append(",");
            sb.append(m.getYearReleased() != null ? m.getYearReleased() : "").append(",");
            sb.append(escapeCsv(m.getGenres())).append(",");
            sb.append(m.getRating() != null ? m.getRating() : "").append(",");
            sb.append(escapeCsv(m.getDirector().getName())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ----------------------------------------------------------------------
    // JSON UPLOAD (Task 1 FORMAT)
    //
    // [
    //   { "title": "...", "director": "...", "year": 2010, "genres": "Sci-Fi" }
    // ]
    // ----------------------------------------------------------------------
    public UploadResult upload(MultipartFile file) throws IOException {

        MovieUploadDto[] movies = objectMapper.readValue(
                file.getInputStream(),
                MovieUploadDto[].class
        );

        int success = 0;
        int failed = 0;

        for (MovieUploadDto dto : movies) {
            try {
                Director director = findDirectorByName(dto.director());

                MovieEntity movie = new MovieEntity();
                movie.setTitle(dto.title());
                movie.setYearReleased(dto.yearReleased());
                movie.setGenres(dto.genres());
                movie.setRating(null); // rating does not exist in upload JSON
                movie.setDirector(director);

                movieRepo.save(movie);
                success++;

            } catch (Exception ex) {
                failed++;
            }
        }

        return new UploadResult(success, failed);
    }

    // ----------------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------------
    private Director findDirectorByName(String name) {
        return directorRepo.findByNameIgnoreCase(name)
                .orElseThrow(() -> new EntityNotFoundException("Director not found: " + name));
    }

    private MovieDetailsDto toDetailsDto(MovieEntity m) {
        return new MovieDetailsDto(
                m.getId(),
                m.getTitle(),
                m.getImage(),
                m.getDescription(),
                m.getYearReleased(),
                m.getGenres(),
                m.getRating(),
                new MovieDetailsDto.DirectorInfo(
                        m.getDirector().getId(),
                        m.getDirector().getName(),
                        m.getDirector().getCountry()
                )
        );
    }

    // result for upload
    public record UploadResult(int successCount, int failedCount) { }
}
