package com.example.user_service.models;

import com.example.user_service.converter.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tv_show_search_history")
@Data
public class TvShowSearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserProfile userProfile;

    private String endpoint;

    @Convert(converter = JsonNodeConverter.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode filters;

    private LocalDateTime searchTime;
}
