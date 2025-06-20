package com.example.tvshows_service.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReviewHelper {
    @Value("${review-rating-service.api.url}")
    private String reviewRatingServiceUrl;

    public String createReviewUrl(Long tvShowId, String username) {
        return reviewRatingServiceUrl + "/api/review/" + tvShowId + "/" + username;
    }
}
