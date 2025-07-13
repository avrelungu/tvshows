package com.example.review_rating_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
    private UUID id;

    private String username;

    private String content;

    private Long tvShowId;

    private Integer rating;

    private Boolean isApproved;

    private Boolean isFlagged;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
