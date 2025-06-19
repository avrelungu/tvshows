package com.example.tvshows_service.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreWatchlistDto {
    private Long tvShowId;
    private String name;
    private String description;
    private String imageMedium;
    private String imageOriginal;
}
