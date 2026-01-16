package com.movies2.director;

import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest(properties = "spring.liquibase.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")

class DirectorControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    DirectorRepository directorRepository;

    private Long directorId;

    @BeforeEach
    void setup() {
        // очистити БД перед кожним тестом
        directorRepository.deleteAll();
        directorRepository.flush();

        // створити директора з відомим ID = 1
        Director d = new Director();
        d.setName("Initial Director");
        d.setCountry("USA");
        directorRepository.saveAndFlush(d);

        directorId = d.getId();
    }

    @Test
    void testCreateDirector() throws Exception {
        mvc.perform(post("/api/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Test Director","country":"USA"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testGetDirectors() throws Exception {
        mvc.perform(get("/api/directors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Initial Director"));
    }

    @Test
    void testUpdateDirector() throws Exception {
        mvc.perform(put("/api/directors/" + directorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Christopher Nolan Updated","country":"UK"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Christopher Nolan Updated"))
                .andExpect(jsonPath("$.country").value("UK"));
    }
}
