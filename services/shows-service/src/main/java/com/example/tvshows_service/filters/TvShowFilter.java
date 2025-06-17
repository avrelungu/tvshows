package com.example.tvshows_service.filters;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TvShowFilter {
    private String name;
    private String description;
    private LocalDateTime premiered;
    private LocalDateTime ended;
    private Integer minRating;
    private Integer maxRating;
    private String status;
    private String network;
    private String language;
    private String sortBy;
    private String sortOrder;
    private List<String> genres;
}
