package com.example.review_rating_service.exceptions;

import org.springframework.http.HttpStatus;

public class ReviewsNotFoundException extends AppException {

    private final HttpStatus httpStatus;

    public ReviewsNotFoundException() {
        super("No reviews found", HttpStatus.NOT_FOUND);

        this.httpStatus = HttpStatus.NOT_FOUND;
    }

    public ReviewsNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);

        this.httpStatus = HttpStatus.NOT_FOUND;
    }
}
