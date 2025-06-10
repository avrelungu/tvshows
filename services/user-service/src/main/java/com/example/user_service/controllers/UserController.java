package com.example.user_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<> getProfile() {

    }

    @GetMapping("/watchlist")
    public ResponseEntity<> getWatchlist() {

    }

    @PostMapping("/upgrade")
    public ResponseEntity<> getUpgrade() {

    }
}
