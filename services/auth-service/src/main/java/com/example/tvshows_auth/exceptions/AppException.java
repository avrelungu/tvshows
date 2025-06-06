package com.example.tvshows_auth.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);

        this.status = status;
    }
}
