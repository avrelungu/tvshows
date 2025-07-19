package com.example.tvshows_auth.exceptions;

import org.springframework.http.HttpStatus;

public class InsufficientPermissionsException extends AppException {

    public InsufficientPermissionsException(String requiredRole, String action) {
        super(String.format("Only %s users can %s", requiredRole, action), HttpStatus.FORBIDDEN);
    }

    public InsufficientPermissionsException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public InsufficientPermissionsException() {
        super("Insufficient permissions to perform this action", HttpStatus.FORBIDDEN);
    }
}