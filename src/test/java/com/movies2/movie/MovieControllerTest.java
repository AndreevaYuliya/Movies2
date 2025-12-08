package com.movies2.movie;

import com.movies2.director.Director;
import com.movies2.director.DirectorRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movies2.movie.dto.MovieRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovieControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    DirectorRepository directorRepository;

    @Autowired
    MovieRepository movieRepository;

    private Long existingDirectorId;
    private Long existingMovieId;


    @BeforeEach
    void setup() {
        movieRepository.deleteAll();
        directorRepository.deleteAll();

        // create director
        Director d = new Director();
        d.setName("Test Director");
        d.setCountry("USA");
        directorRepository.saveAndFlush(d);

        // create movie
        MovieEntity m = new MovieEntity();
        m.setTitle("Test Movie");
        m.setYearReleased(2020);
        m.setGenres("Action");
        m.setRating(8.5);
        m.setDirector(d);
        movieRepository.saveAndFlush(m);

        // save generated IDs
        existingDirectorId = d.getId();
        existingMovieId = m.getId();
    }


    @Test
    void testCreateMovie() throws Exception {

        MovieRequestDto dto = new MovieRequestDto(
                "Interstellar",
                2014,
                "Sci-Fi",
                8.5,
                existingDirectorId   // FIX
        );

        mvc.perform(
                        post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Interstellar"));
    }

    @Test
    void testGetMovieById() throws Exception {
        mvc.perform(get("/api/movies/" + existingMovieId))  // FIX
                .andExpect(status().isOk());
    }

    @Test
    void testUploadMovies() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "movies.json",
                "application/json",
                """
                        [
                            {"title":"Test","director":"Test Director","year":2010,"genres":"Sci-Fi"}
                        ]
                        """.getBytes()
        );

        mvc.perform(multipart("/api/movies/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1));
    }
}