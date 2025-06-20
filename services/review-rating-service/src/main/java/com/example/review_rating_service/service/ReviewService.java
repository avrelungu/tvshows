package com.example.review_rating_service.service;

import com.example.review_rating_service.dto.ReviewDto;
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

    public void storeReview(StoreReviewDto storeReviewDto, Long tvShowId, String username) {
        Review review = reviewMapper.toModel(storeReviewDto);

        review.setUsername(username);
        review.setTvShowId(tvShowId);

        reviewRepository.save(review);
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
}
