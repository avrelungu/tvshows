package com.example.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class AlreadyPremiumMemberException extends AppException {

    public AlreadyPremiumMemberException(String username) {
        super(username + " already has a PREMIUM membership", HttpStatus.BAD_REQUEST);
    }

    public AlreadyPremiumMemberException() {
        super("User already has a PREMIUM membership", HttpStatus.BAD_REQUEST);
    }
}