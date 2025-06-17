package com.example.tvshows_service.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "tv_shows")
public class TvShow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tvshow_id", nullable = false, unique = true)
    private Long tvShowId;
    private String name;
    private String type;
    private String language;
    private String status;
    private int runtime;
    private int averageRuntime;
    @Column(name = "premiered")
    private LocalDate premiered;
    @Column(name = "ended")
    private LocalDate ended;
    private String officialSite;
    private double rating;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tv_show_genres",
            joinColumns = @JoinColumn(name = "tv_show_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @EqualsAndHashCode.Exclude
    private Set<Genre> genres;

    // Flattened schedule details.
    private String scheduleTime;
    private List<String> scheduleDays;

    // Flattened externals details.
    private int tvrage;
    private int thetvdb;
    private String imdb;

    // Image URLs stored directly.
    private String imageMedium;
    private String imageOriginal;

    private String summary;
}
