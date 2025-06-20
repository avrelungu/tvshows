package com.example.review_rating_service.mapper;

import com.example.review_rating_service.dto.ReviewDto;
import com.example.review_rating_service.dto.StoreReviewDto;
import com.example.review_rating_service.models.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    Review toModel(StoreReviewDto storeReviewDto);

    ReviewDto toDto(Review review);
}
