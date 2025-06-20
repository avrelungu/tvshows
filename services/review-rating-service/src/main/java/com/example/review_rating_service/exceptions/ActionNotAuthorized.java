package com.example.review_rating_service.exceptions;

import org.springframework.http.HttpStatus;

public class ActionNotAuthorized extends AppException {
    private final HttpStatus httpStatus;

    public ActionNotAuthorized() {
        super("You do not have permission to perform this action.", HttpStatus.UNAUTHORIZED);

        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }

    public ActionNotAuthorized(String message) {
        super(message, HttpStatus.UNAUTHORIZED);

        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
}
