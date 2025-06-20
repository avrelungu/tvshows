package com.example.review_rating_service.exceptions;

import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends AppException {
    private final HttpStatus httpStatus;

    public ReviewNotFoundException() {
        super("Review not found", HttpStatus.NOT_FOUND);

        this.httpStatus = HttpStatus.NOT_FOUND;
    }

    public ReviewNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);

        this.httpStatus = HttpStatus.NOT_FOUND;
    }
}
