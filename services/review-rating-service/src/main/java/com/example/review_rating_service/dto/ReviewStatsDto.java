package com.example.review_rating_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewStatsDto {
    private Long tvShowId;
    
    private Double averageRating;
    
    private Long totalReviews;
}