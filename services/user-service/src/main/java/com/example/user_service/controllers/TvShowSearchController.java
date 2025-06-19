package com.example.user_service.controllers;

import com.example.user_service.dto.StoreShowsSearchDto;
import com.example.user_service.dto.TvShowSearchHistoryDto;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.models.TvShowSearchHistory;
import com.example.user_service.services.TvShowSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/api/users/search")
public class TvShowSearchController {

    private final TvShowSearchService tvShowSearchService;

    public TvShowSearchController(TvShowSearchService tvShowSearchService) {
        this.tvShowSearchService = tvShowSearchService;
    }

    @PostMapping("/store/{username}")
    public ResponseEntity<Void> storeSearch(
            @PathVariable String username,
            @RequestBody StoreShowsSearchDto storeShowsSearchDto
    ) throws UserProfileNotFoundException {
        tvShowSearchService.storeTvShowSearch(username, storeShowsSearchDto);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<TvShowSearchHistoryDto>> getSearch(@PathVariable String username) throws UserProfileNotFoundException {
        List<TvShowSearchHistoryDto> tvShowSearchHistoryDtoList = tvShowSearchService.getSearchHistory(username);

        return ResponseEntity.ok(tvShowSearchHistoryDtoList);
    }
}
