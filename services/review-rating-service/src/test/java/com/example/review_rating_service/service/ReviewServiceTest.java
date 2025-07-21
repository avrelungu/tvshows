package com.example.review_rating_service.service;

import com.example.review_rating_service.dto.ReviewDto;
import com.example.review_rating_service.dto.ReviewStatsDto;
import com.example.review_rating_service.dto.StoreReviewDto;
import com.example.review_rating_service.dto.UpdateReviewDto;
import com.example.review_rating_service.enums.Membership;
import com.example.review_rating_service.enums.Role;
import com.example.review_rating_service.exceptions.ActionNotAuthorized;
import com.example.review_rating_service.exceptions.ReviewNotFoundException;
import com.example.review_rating_service.exceptions.ReviewsNotFoundException;
import com.example.review_rating_service.mapper.ReviewMapper;
import com.example.review_rating_service.models.Review;
import com.example.review_rating_service.repositories.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    private StoreReviewDto storeReviewDto;
    private UpdateReviewDto updateReviewDto;
    private Review review;
    private ReviewDto reviewDto;
    private UUID reviewId;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();

        storeReviewDto = StoreReviewDto.builder()
                .content("Great show!")
                .rating(5)
                .build();

        updateReviewDto = UpdateReviewDto.builder()
                .content("Updated review content")
                .rating(4)
                .build();

        review = new Review();
        review.setId(reviewId);
        review.setTvShowId(1L);
        review.setUsername("testuser");
        review.setContent("Great show!");
        review.setRating(5);
        review.setIsApproved(false);
        review.setIsFlagged(false);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        reviewDto = ReviewDto.builder()
                .id(reviewId)
                .tvShowId(1L)
                .username("testuser")
                .content("Great show!")
                .rating(5)
                .isApproved(false)
                .isFlagged(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void storeReview_ShouldStoreSuccessfully_WhenPremiumUser() throws ActionNotAuthorized {
        when(reviewMapper.toModel(storeReviewDto)).thenReturn(review);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.storeReview(storeReviewDto, 1L, "testuser", "USER", "PREMIUM");

        verify(reviewMapper).toModel(storeReviewDto);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void storeReview_ShouldThrowActionNotAuthorized_WhenFreeUser() {
        assertThrows(ActionNotAuthorized.class, () -> 
            reviewService.storeReview(storeReviewDto, 1L, "testuser", "USER", "FREE"));

        verifyNoInteractions(reviewMapper);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void storeReview_ShouldSetFlaggedTrue_WhenAdminUser() throws ActionNotAuthorized {
        Review savedReview = new Review();
        when(reviewMapper.toModel(storeReviewDto)).thenReturn(savedReview);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        reviewService.storeReview(storeReviewDto, 1L, "admin", "ADMIN", "PREMIUM");

        verify(reviewRepository).save(argThat(r -> r.getIsFlagged().equals(true)));
    }

    @Test
    void storeReview_ShouldThrowIllegalArgumentException_WhenDataIntegrityViolation() {
        when(reviewMapper.toModel(storeReviewDto)).thenReturn(review);
        when(reviewRepository.save(any(Review.class))).thenThrow(new DataIntegrityViolationException("Duplicate"));

        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.storeReview(storeReviewDto, 1L, "testuser", "USER", "PREMIUM"));

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void getReviews_ShouldReturnReviews_WhenUserHasReviews() throws ReviewsNotFoundException {
        List<Review> reviews = List.of(review);
        when(reviewRepository.findAllByUsername("testuser")).thenReturn(reviews);
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        List<ReviewDto> result = reviewService.getReviews("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findAllByUsername("testuser");
        verify(reviewMapper).toDto(review);
    }

    @Test
    void getReviews_ShouldThrowReviewsNotFoundException_WhenUserHasNoReviews() {
        when(reviewRepository.findAllByUsername("testuser")).thenReturn(new ArrayList<>());

        assertThrows(ReviewsNotFoundException.class, () -> 
            reviewService.getReviews("testuser"));

        verify(reviewRepository).findAllByUsername("testuser");
        verifyNoInteractions(reviewMapper);
    }

    @Test
    void getReview_ShouldUpdateAndReturnReview_WhenAdminUser() throws ReviewNotFoundException, ActionNotAuthorized {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        ReviewDto result = reviewService.getReview(reviewId, updateReviewDto, "ADMIN");

        assertNotNull(result);
        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).save(any(Review.class));
        verify(reviewMapper).toDto(review);
    }

    @Test
    void getReview_ShouldThrowActionNotAuthorized_WhenNonAdminUser() {
        assertThrows(ActionNotAuthorized.class, () -> 
            reviewService.getReview(reviewId, updateReviewDto, "USER"));

        verifyNoInteractions(reviewRepository);
        verifyNoInteractions(reviewMapper);
    }

    @Test
    void getReview_ShouldThrowReviewNotFoundException_WhenReviewNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> 
            reviewService.getReview(reviewId, updateReviewDto, "ADMIN"));

        verify(reviewRepository).findById(reviewId);
    }

    @Test
    void getReviewsByTvShow_ShouldReturnPageOfReviews() {
        List<Review> reviews = List.of(review);
        Page<Review> reviewsPage = new PageImpl<>(reviews);
        when(reviewRepository.findAllByTvShowIdAndIsApprovedTrue(eq(1L), any(Pageable.class))).thenReturn(reviewsPage);
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        Page<ReviewDto> result = reviewService.getReviewsByTvShow(1L, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reviewRepository).findAllByTvShowIdAndIsApprovedTrue(eq(1L), any(Pageable.class));
    }

    @Test
    void getReviewStats_ShouldReturnStats_WhenDataExists() {
        when(reviewRepository.findAverageRatingByTvShowId(1L)).thenReturn(4.5);
        when(reviewRepository.countApprovedReviewsByTvShowId(1L)).thenReturn(10L);

        ReviewStatsDto result = reviewService.getReviewStats(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTvShowId());
        assertEquals(4.5, result.getAverageRating());
        assertEquals(10L, result.getTotalReviews());
        verify(reviewRepository).findAverageRatingByTvShowId(1L);
        verify(reviewRepository).countApprovedReviewsByTvShowId(1L);
    }

    @Test
    void getReviewStats_ShouldReturnZeroDefaults_WhenNoDataExists() {
        when(reviewRepository.findAverageRatingByTvShowId(1L)).thenReturn(null);
        when(reviewRepository.countApprovedReviewsByTvShowId(1L)).thenReturn(null);

        ReviewStatsDto result = reviewService.getReviewStats(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTvShowId());
        assertEquals(0.0, result.getAverageRating());
        assertEquals(0L, result.getTotalReviews());
    }

    @Test
    void getUserReviewForShow_ShouldReturnReview_WhenExists() throws ReviewNotFoundException {
        when(reviewRepository.findByTvShowIdAndUsername(1L, "testuser")).thenReturn(Optional.of(review));
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        ReviewDto result = reviewService.getUserReviewForShow(1L, "testuser");

        assertNotNull(result);
        assertEquals(reviewDto, result);
        verify(reviewRepository).findByTvShowIdAndUsername(1L, "testuser");
        verify(reviewMapper).toDto(review);
    }

    @Test
    void getUserReviewForShow_ShouldThrowReviewNotFoundException_WhenNotExists() {
        when(reviewRepository.findByTvShowIdAndUsername(1L, "testuser")).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> 
            reviewService.getUserReviewForShow(1L, "testuser"));

        verify(reviewRepository).findByTvShowIdAndUsername(1L, "testuser");
        verifyNoInteractions(reviewMapper);
    }

    @Test
    void deleteReview_ShouldDeleteSuccessfully_WhenAdminUser() throws ReviewNotFoundException, ActionNotAuthorized {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.deleteReview(reviewId, "anyuser", "ADMIN");

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_ShouldDeleteSuccessfully_WhenOwnerUser() throws ReviewNotFoundException, ActionNotAuthorized {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.deleteReview(reviewId, "testuser", "USER");

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_ShouldThrowActionNotAuthorized_WhenNonOwnerUser() {
        review.setUsername("otheruser");
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        assertThrows(ActionNotAuthorized.class, () -> 
            reviewService.deleteReview(reviewId, "testuser", "USER"));

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReview_ShouldThrowReviewNotFoundException_WhenReviewNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> 
            reviewService.deleteReview(reviewId, "testuser", "USER"));

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void getPendingReviews_ShouldReturnPendingReviews_WhenAdminUser() throws ActionNotAuthorized {
        List<Review> pendingReviews = List.of(review);
        when(reviewRepository.findAllByIsApprovedFalse()).thenReturn(pendingReviews);
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        List<ReviewDto> result = reviewService.getPendingReviews("ADMIN");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findAllByIsApprovedFalse();
        verify(reviewMapper).toDto(review);
    }

    @Test
    void getPendingReviews_ShouldThrowActionNotAuthorized_WhenNonAdminUser() {
        assertThrows(ActionNotAuthorized.class, () -> 
            reviewService.getPendingReviews("USER"));

        verifyNoInteractions(reviewRepository);
        verifyNoInteractions(reviewMapper);
    }

    @Test
    void approveReview_ShouldApproveSuccessfully_WhenAdminUser() throws ReviewNotFoundException, ActionNotAuthorized {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.approveReview(reviewId, "ADMIN");

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).save(argThat(r -> r.getIsApproved().equals(true) && r.getIsFlagged().equals(false)));
    }

    @Test
    void approveReview_ShouldThrowActionNotAuthorized_WhenNonAdminUser() {
        assertThrows(ActionNotAuthorized.class, () -> 
            reviewService.approveReview(reviewId, "USER"));

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void approveReview_ShouldThrowReviewNotFoundException_WhenReviewNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> 
            reviewService.approveReview(reviewId, "ADMIN"));

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void rejectReview_ShouldDeleteSuccessfully_WhenAdminUser() throws ReviewNotFoundException, ActionNotAuthorized {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.rejectReview(reviewId, "ADMIN");

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).delete(review);
    }

    @Test
    void rejectReview_ShouldThrowActionNotAuthorized_WhenNonAdminUser() {
        assertThrows(ActionNotAuthorized.class, () -> 
            reviewService.rejectReview(reviewId, "USER"));

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void rejectReview_ShouldThrowReviewNotFoundException_WhenReviewNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> 
            reviewService.rejectReview(reviewId, "ADMIN"));

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void storeReview_ShouldSetCorrectReviewProperties() throws ActionNotAuthorized {
        Review capturedReview = new Review();
        when(reviewMapper.toModel(storeReviewDto)).thenReturn(capturedReview);
        when(reviewRepository.save(any(Review.class))).thenReturn(capturedReview);

        reviewService.storeReview(storeReviewDto, 1L, "testuser", "USER", "PREMIUM");

        verify(reviewRepository).save(argThat(r -> 
            r.getUsername().equals("testuser") &&
            r.getTvShowId().equals(1L) &&
            r.getIsApproved().equals(false) &&
            r.getIsFlagged().equals(false)
        ));
    }

    @Test
    void getReviewsByTvShow_ShouldReturnEmptyPage_WhenNoReviews() {
        Page<Review> emptyPage = new PageImpl<>(new ArrayList<>());
        when(reviewRepository.findAllByTvShowIdAndIsApprovedTrue(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        Page<ReviewDto> result = reviewService.getReviewsByTvShow(1L, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(reviewRepository).findAllByTvShowIdAndIsApprovedTrue(eq(1L), any(Pageable.class));
    }
}