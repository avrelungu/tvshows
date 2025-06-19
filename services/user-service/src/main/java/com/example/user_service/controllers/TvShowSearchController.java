package com.example.user_service.controllers;

import com.example.user_service.dto.StoreShowsSearchDto;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.services.TvShowSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/tv-shows")
public class TvShowSearchController {

    private TvShowSearchService watchlistService;

    public TvShowSearchController(TvShowSearchService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostMapping("/store-search")
    public ResponseEntity<Void> storeSearch(
            @RequestBody StoreShowsSearchDto storeShowsSearchDto
    ) throws UserProfileNotFoundException {
        watchlistService.storeTvShowSearch(storeShowsSearchDto);

        return ResponseEntity.ok().build();
    }
}
