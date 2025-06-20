package com.example.review_rating_service.controllers;

import com.example.review_rating_service.dto.ReviewDto;
import com.example.review_rating_service.dto.StoreReviewDto;
import com.example.review_rating_service.dto.UpdateReviewDto;
import com.example.review_rating_service.exceptions.ActionNotAuthorized;
import com.example.review_rating_service.exceptions.ReviewNotFoundException;
import com.example.review_rating_service.exceptions.ReviewsNotFoundException;
import com.example.review_rating_service.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{tvShowId}/{username}")
    public ResponseEntity<Void> storeReview(
            @RequestBody StoreReviewDto storeReviewDto,
            @PathVariable Long tvShowId,
            @PathVariable String username
    ) {
        reviewService.storeReview(storeReviewDto, tvShowId, username);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<ReviewDto>> getReviews(
            @PathVariable String username
    ) throws ReviewsNotFoundException {
        List<ReviewDto> reviewDtoList = reviewService.getReviews(username);

        return ResponseEntity.ok(reviewDtoList);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReviewId(
            @PathVariable UUID reviewId,
            @RequestBody UpdateReviewDto updateReviewDto,
            @RequestHeader(name = "X-Auth-Role", defaultValue = "") String role
    ) throws ReviewNotFoundException, ActionNotAuthorized {
        ReviewDto review = reviewService.getReview(reviewId, updateReviewDto, role);

        return ResponseEntity.ok(review);
    }
}
