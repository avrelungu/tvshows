package com.example.tvshows_service.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private String sortBy = "id";
    private String sortOrder = "asc";
    private List<String> genres;

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                name, description, network, status,
                ended, premiered, language,
                minRating, maxRating, genres,
                sortBy, sortOrder
        );
    }
}
