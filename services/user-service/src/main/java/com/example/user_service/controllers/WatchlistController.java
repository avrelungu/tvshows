package com.example.user_service.controllers;

import com.example.user_service.dto.StoreShowsSearchDto;
import com.example.user_service.dto.StoreWatchlistDto;
import com.example.user_service.dto.WatchlistDto;
import com.example.user_service.exceptions.AppException;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.services.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<WatchlistDto>> getWatchlist(
            @PathVariable String username
    ) throws UserProfileNotFoundException {
        List<WatchlistDto> watchlistDtos = watchlistService.getWatchlistForUserProfile(username);

        return ResponseEntity.ok(watchlistDtos);
    }

    @PostMapping("/{username}")
    public ResponseEntity<Void> addToWatchList(
            @PathVariable String username,
            @RequestBody StoreWatchlistDto storeWatchlistDto
    ) throws AppException {
        watchlistService.addToWatchList(username, storeWatchlistDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/{tvShowId}")
    public void removeFromWatchList(
            @PathVariable String username,
            @PathVariable String tvShowId
    ) throws AppException {
        watchlistService.removeFromWatchList(username, tvShowId);
    }
}
