package com.example.tvshows_service.dto.external;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreTvShowSearchDto {
    private String endpoint;
    private JsonNode filters;
}
