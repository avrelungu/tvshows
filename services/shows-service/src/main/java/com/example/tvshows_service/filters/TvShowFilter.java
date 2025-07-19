package com.example.tvshows_service.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TvShowFilter {
    private String name;
    private String description;
    private LocalDate premiered;
    private LocalDate ended;
    private Integer minRating;
    private Integer maxRating;
    private String status;
    private String network;
    private String language;
    private String sortBy = "id";
    private String sortOrder = "asc";
    private List<String> genres;
    private List<Long> ids;

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
