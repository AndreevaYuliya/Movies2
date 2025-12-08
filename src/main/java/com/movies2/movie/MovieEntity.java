package com.movies2.movie;

import com.movies2.director.Director;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(
        name = "movies",
        indexes = {
                @Index(name = "idx_movie_director", columnList = "director_id"),
                @Index(name = "idx_movie_yearReleased", columnList = "yearReleased")
        }
)
public class MovieEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "year_released")
    private Integer yearReleased;

    private String genres;

    // getters/setters

    private Double rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movies_director"))
    private Director director;


}
