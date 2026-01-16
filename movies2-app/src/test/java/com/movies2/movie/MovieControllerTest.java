package com.movies2.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movies2.director.Director;
import com.movies2.director.DirectorRepository;
import com.movies2.movie.dto.MovieRequestDto;
import com.movies2.service.EmailProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovieControllerTest {
    @MockBean
    EmailProducer emailProducer;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DirectorRepository directorRepository;

    @Autowired
    MovieRepository movieRepository;

    private Long existingDirectorId;
    private Long existingMovieId;

    @BeforeEach
    void setup() {
        // clean DB
        movieRepository.deleteAll();
        directorRepository.deleteAll();

        // create director
        Director director = new Director();
        director.setName("Test Director");
        director.setCountry("USA");
        director = directorRepository.saveAndFlush(director);
        existingDirectorId = director.getId();

        // create movie
        MovieEntity movie = new MovieEntity();
        movie.setTitle("Test Movie");
        movie.setImage("");
        movie.setDescription("");
        movie.setYearReleased(2020);
        movie.setGenres("Action");
        movie.setRating(8.5);
        movie.setDirector(director);
        movie = movieRepository.saveAndFlush(movie);
        existingMovieId = movie.getId();
    }

    // ----------------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------------
    @Test
    void testCreateMovie() throws Exception {

        MovieRequestDto dto = new MovieRequestDto(
                "Interstellar",
                "",
                "Sci-Fi movie",
                2014,
                "Sci-Fi",
                8.7,
                existingDirectorId
        );

        mvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Interstellar"));
    }

    // ----------------------------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------------------------
    @Test
    void testGetMovieById() throws Exception {
        mvc.perform(get("/api/movies/" + existingMovieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingMovieId))
                .andExpect(jsonPath("$.title").value("Test Movie"));
    }

    // ----------------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------------
    @Test
    void testUpdateMovie() throws Exception {

        MovieRequestDto dto = new MovieRequestDto(
                "Updated Movie",
                "",
                "Updated description",
                2021,
                "Drama",
                9.0,
                existingDirectorId
        );

        mvc.perform(put("/api/movies/" + existingMovieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Movie"))
                .andExpect(jsonPath("$.yearReleased").value(2021));
    }

    // ----------------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------------
    @Test
    void testDeleteMovie() throws Exception {
        mvc.perform(delete("/api/movies/" + existingMovieId))
                .andExpect(status().isNoContent());
    }

    // ----------------------------------------------------------------------
    // LIST + PAGINATION
    // ----------------------------------------------------------------------
    @Test
    void testListMovies() throws Exception {
        mvc.perform(post("/api/movies/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "page": 0,
                              "size": 10
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    // ----------------------------------------------------------------------
    // UPLOAD
    // ----------------------------------------------------------------------
    @Test
    void testUploadMovies() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "movies.json",
                "application/json",
                """
                [
                  {
                    "title": "Uploaded Movie",
                    "director": "Test Director",
                    "yearReleased": 2010,
                    "genres": "Sci-Fi"
                  }
                ]
                """.getBytes()
        );

        mvc.perform(multipart("/api/movies/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failedCount").value(0));
    }
}
