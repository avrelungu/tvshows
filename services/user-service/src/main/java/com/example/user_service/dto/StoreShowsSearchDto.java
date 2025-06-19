package com.example.user_service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StoreShowsSearchDto {
    private UUID userId;
    private String endpoint;
    private JsonNode filters;
    private LocalDateTime searchTime;
}
