package com.example.user_service.controllers;

import com.example.user_service.dto.WatchlistDto;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.services.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<WatchlistDto>> getWatchlist(@PathVariable String username) throws UserProfileNotFoundException {
        List<WatchlistDto> watchlistDtos = watchlistService.getWatchlistForUserProfile(username);

        return ResponseEntity.ok(watchlistDtos);
    }
}
