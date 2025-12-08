package com.movies2.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movies2.movie.dto.MovieRequestDto;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers;

import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource("classpath:application-test.properties")
class MovieControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void testCreateMovie() throws Exception {

        MovieRequestDto dto = new MovieRequestDto(
                "Interstellar",
                2014,
                "Sci-Fi",
                8.5,
                1L // director_id from Liquibase seed
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
        mvc.perform(get("/api/movies/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testListMovies() throws Exception {
        mvc.perform(post("/api/movies/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"page":0, "size":10}
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    void testReport() throws Exception {
        mvc.perform(post("/api/movies/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        Matchers.containsString("movies_report.csv")));
    }

    @Test
    void testUploadMovies() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "movies.json",
                "application/json",
                """
                [
                    {"title":"Test","director":"Christopher Nolan","year":2010,"genres":"Sci-Fi"}
                ]
                """.getBytes()
        );

        mvc.perform(multipart("/api/movies/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1));
    }

}
