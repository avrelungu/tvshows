package com.example.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class UserProfileNotFoundException extends AppException {

    public UserProfileNotFoundException(String exceptionMessage) {
        super(exceptionMessage, HttpStatus.NOT_FOUND);
    }

    public UserProfileNotFoundException(String exceptionMessage, HttpStatus httpStatus) {
        super(exceptionMessage, httpStatus);
    }

    public UserProfileNotFoundException() {
        super("User Profile does not exist", HttpStatus.NOT_FOUND);
    }
}
