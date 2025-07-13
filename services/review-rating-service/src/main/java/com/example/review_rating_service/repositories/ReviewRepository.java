package com.example.review_rating_service.repositories;

import com.example.review_rating_service.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findAllByUsername(String username);
    
    Page<Review> findAllByTvShowIdAndIsApprovedTrue(Long tvShowId, Pageable pageable);
    
    Optional<Review> findByTvShowIdAndUsername(Long tvShowId, String username);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tvShowId = :tvShowId AND r.isApproved = true")
    Double findAverageRatingByTvShowId(@Param("tvShowId") Long tvShowId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.tvShowId = :tvShowId AND r.isApproved = true")
    Long countApprovedReviewsByTvShowId(@Param("tvShowId") Long tvShowId);
    
    List<Review> findAllByIsApprovedFalse();
    
    List<Review> findAllByIsFlaggedTrue();
}
