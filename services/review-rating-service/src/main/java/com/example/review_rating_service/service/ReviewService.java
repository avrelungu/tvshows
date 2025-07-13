package com.example.review_rating_service.service;

import com.example.review_rating_service.dto.ReviewDto;
import com.example.review_rating_service.dto.ReviewStatsDto;
import com.example.review_rating_service.dto.StoreReviewDto;
import com.example.review_rating_service.dto.UpdateReviewDto;
import com.example.review_rating_service.enums.Role;
import com.example.review_rating_service.exceptions.ActionNotAuthorized;
import com.example.review_rating_service.exceptions.ReviewNotFoundException;
import com.example.review_rating_service.exceptions.ReviewsNotFoundException;
import com.example.review_rating_service.mapper.ReviewMapper;
import com.example.review_rating_service.models.Review;
import com.example.review_rating_service.repositories.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewMapper reviewMapper, ReviewRepository reviewRepository) {
        this.reviewMapper = reviewMapper;
        this.reviewRepository = reviewRepository;
    }

    public void storeReview(StoreReviewDto storeReviewDto, Long tvShowId, String username, String role) throws ActionNotAuthorized {
        Role userRole = Role.valueOf(role);
        if (!userRole.equals(Role.PREMIUM) && !userRole.equals(Role.ADMIN)) {
            throw new ActionNotAuthorized();
        }

        Review review = reviewMapper.toModel(storeReviewDto);
        review.setUsername(username);
        review.setTvShowId(tvShowId);
        review.setIsApproved(false); // Reviews need admin approval
        review.setIsFlagged(false);  // Not flagged by default

        try {
            reviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("User already has a review for this TV show");
        }
    }

    public List<ReviewDto> getReviews(String username) throws ReviewsNotFoundException {
        List<Review> reviewList = reviewRepository.findAllByUsername(username);

        if (reviewList.isEmpty()) {
            throw new ReviewsNotFoundException();
        }

        return reviewList.stream().map(reviewMapper::toDto).toList();
    }

    public ReviewDto getReview(UUID reviewId, UpdateReviewDto updateReviewDto, String role) throws ReviewNotFoundException, ActionNotAuthorized {
        log.info("Get review role {}", role);
        if (!Role.valueOf(role).equals(Role.ADMIN)) {
            throw new ActionNotAuthorized();
        }

        Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

        review.setContent(updateReviewDto.getContent());
        review.setRating(updateReviewDto.getRating());

        reviewRepository.save(review);

        return reviewMapper.toDto(review);
    }

    public Page<ReviewDto> getReviewsByTvShow(Long tvShowId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAllByTvShowIdAndIsApprovedTrue(tvShowId, pageable);
        return reviews.map(reviewMapper::toDto);
    }

    public ReviewStatsDto getReviewStats(Long tvShowId) {
        Double averageRating = reviewRepository.findAverageRatingByTvShowId(tvShowId);
        Long totalReviews = reviewRepository.countApprovedReviewsByTvShowId(tvShowId);
        
        return ReviewStatsDto.builder()
                .tvShowId(tvShowId)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .build();
    }

    public ReviewDto getUserReviewForShow(Long tvShowId, String username) throws ReviewNotFoundException {
        Review review = reviewRepository.findByTvShowIdAndUsername(tvShowId, username)
                .orElseThrow(ReviewNotFoundException::new);
        return reviewMapper.toDto(review);
    }

    public void deleteReview(UUID reviewId, String username, String role) throws ReviewNotFoundException, ActionNotAuthorized {
        Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
        
        Role userRole = Role.valueOf(role);
        if (!userRole.equals(Role.ADMIN) && !review.getUsername().equals(username)) {
            throw new ActionNotAuthorized();
        }
        
        reviewRepository.delete(review);
    }

    public List<ReviewDto> getPendingReviews(String role) throws ActionNotAuthorized {
        if (!Role.valueOf(role).equals(Role.ADMIN)) {
            throw new ActionNotAuthorized();
        }
        
        List<Review> pendingReviews = reviewRepository.findAllByIsApprovedFalse();
        return pendingReviews.stream().map(reviewMapper::toDto).toList();
    }

    public void approveReview(UUID reviewId, String role) throws ReviewNotFoundException, ActionNotAuthorized {
        if (!Role.valueOf(role).equals(Role.ADMIN)) {
            throw new ActionNotAuthorized();
        }
        
        Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
        review.setIsApproved(true);
        review.setIsFlagged(false);
        reviewRepository.save(review);
    }

    public void rejectReview(UUID reviewId, String role) throws ReviewNotFoundException, ActionNotAuthorized {
        if (!Role.valueOf(role).equals(Role.ADMIN)) {
            throw new ActionNotAuthorized();
        }
        
        Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
        reviewRepository.delete(review);
    }
}
