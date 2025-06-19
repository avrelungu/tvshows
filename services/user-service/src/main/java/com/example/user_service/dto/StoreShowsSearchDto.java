package com.example.user_service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreShowsSearchDto {
    private String endpoint;
    private JsonNode filters;
    private LocalDateTime searchTime = LocalDateTime.now();
}
