package com.example.user_service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class AppException extends Throwable {
    @Getter
    private final HttpStatus status;

    public AppException(String exceptionMessage, HttpStatus httpStatus) {
        super(exceptionMessage);

        this.status = httpStatus;
    }
}
