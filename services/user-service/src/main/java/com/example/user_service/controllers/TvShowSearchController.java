package com.example.user_service.controllers;

import com.example.user_service.dto.StoreShowsSearchDto;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.services.TvShowSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/users/search")
public class TvShowSearchController {

    private TvShowSearchService watchlistService;

    public TvShowSearchController(TvShowSearchService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostMapping("/store/{username}")
    public ResponseEntity<Void> storeSearch(
            @PathVariable String username,
            @RequestBody StoreShowsSearchDto storeShowsSearchDto
    ) throws UserProfileNotFoundException {
        watchlistService.storeTvShowSearch(username, storeShowsSearchDto);

        return ResponseEntity.ok().build();
    }
}
