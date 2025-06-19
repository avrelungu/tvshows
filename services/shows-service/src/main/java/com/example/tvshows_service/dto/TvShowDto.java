package com.example.tvshows_service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TvShowDto extends RepresentationModel<TvShowDto> {
    private Long id;
    private String name;
    private String language;
    private String status;
    private double rating;

    // Image URLs.
    private String imageMedium;
    private String imageOriginal;

    private String summary;
    private List<String> genres;

    // Flattened schedule details.
    private String scheduleTime;
    private List<String> scheduleDays;

    // Additional details.
    private int runtime;
    private int averageRuntime;
    private String premiered;
    private String ended;
    private String officialSite;

    // Flattened externals.
    private int tvrage;
    private int thetvdb;
    private String imdb;

    // Add to Watchlist
    private String watchlistUrl;
}
