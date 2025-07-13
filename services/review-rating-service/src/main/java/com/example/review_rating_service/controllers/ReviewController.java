package com.example.review_rating_service.controllers;

import com.example.review_rating_service.dto.ReviewDto;
import com.example.review_rating_service.dto.ReviewStatsDto;
import com.example.review_rating_service.dto.StoreReviewDto;
import com.example.review_rating_service.dto.UpdateReviewDto;
import com.example.review_rating_service.exceptions.ActionNotAuthorized;
import com.example.review_rating_service.exceptions.ReviewNotFoundException;
import com.example.review_rating_service.exceptions.ReviewsNotFoundException;
import com.example.review_rating_service.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            @PathVariable String username,
            @RequestHeader(name = "X-Auth-Role", defaultValue = "") String role
    ) throws ActionNotAuthorized {
        reviewService.storeReview(storeReviewDto, tvShowId, username, role);

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

    @GetMapping("/tv-show/{tvShowId}")
    public ResponseEntity<Page<ReviewDto>> getReviewsByTvShow(
            @PathVariable Long tvShowId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ReviewDto> reviews = reviewService.getReviewsByTvShow(tvShowId, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/tv-show/{tvShowId}/stats")
    public ResponseEntity<ReviewStatsDto> getReviewStats(@PathVariable Long tvShowId) {
        ReviewStatsDto stats = reviewService.getReviewStats(tvShowId);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/tv-show/{tvShowId}/user/{username}")
    public ResponseEntity<ReviewDto> getUserReviewForShow(
            @PathVariable Long tvShowId,
            @PathVariable String username
    ) throws ReviewNotFoundException {
        ReviewDto review = reviewService.getUserReviewForShow(tvShowId, username);
        
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @RequestHeader(name = "X-Auth-Username", defaultValue = "") String username,
            @RequestHeader(name = "X-Auth-Role", defaultValue = "") String role
    ) throws ReviewNotFoundException, ActionNotAuthorized {
        reviewService.deleteReview(reviewId, username, role);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ReviewDto>> getPendingReviews(
            @RequestHeader(name = "X-Auth-Role", defaultValue = "") String role
    ) throws ActionNotAuthorized {
        List<ReviewDto> pendingReviews = reviewService.getPendingReviews(role);
        
        return ResponseEntity.ok(pendingReviews);
    }

    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<Void> approveReview(
            @PathVariable UUID reviewId,
            @RequestHeader(name = "X-Auth-Role", defaultValue = "") String role
    ) throws ReviewNotFoundException, ActionNotAuthorized {
        reviewService.approveReview(reviewId, role);
        
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{reviewId}/reject")
    public ResponseEntity<Void> rejectReview(
            @PathVariable UUID reviewId,
            @RequestHeader(name = "X-Auth-Role", defaultValue = "") String role
    ) throws ReviewNotFoundException, ActionNotAuthorized {
        reviewService.rejectReview(reviewId, role);
        
        return ResponseEntity.noContent().build();
    }
}
