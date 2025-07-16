package com.example.tvshows_service.controllers;

import com.example.tvshows_service.dto.TvShowDto;
import com.example.tvshows_service.exceptions.TvShowsNotFoundException;
import com.example.tvshows_service.filters.TvShowFilter;
import com.example.tvshows_service.service.TvShowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tv-shows")
public class TvShowsController {
    private final TvShowService tvShowService;

    public TvShowsController(
            TvShowService tvShowService
    ) {
        this.tvShowService = tvShowService;
    }

    @GetMapping
    public ResponseEntity<Page<TvShowDto>> getTvShows(
            @ModelAttribute TvShowFilter tvShowFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(name = "X-Auth-Username", defaultValue = "") String username,
            HttpServletRequest request
    ) throws TvShowsNotFoundException {
        Page<TvShowDto> tvShowPage = tvShowService.getTvShows(page, size, tvShowFilter, username);

        tvShowService.storeFilteredTvShowsSearchHistory(username, request.getRequestURL().toString(), tvShowFilter);

        return ResponseEntity.ok(tvShowPage);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<Page<TvShowDto>> topRatedShows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(name = "X-Auth-Username", defaultValue = "") String username
    ) throws TvShowsNotFoundException {
        Page<TvShowDto> showList = tvShowService.getTopRatedShows(page, size, username);

        return ResponseEntity.ok(showList);
    }

    @GetMapping("/{tvShowId}")
    public ResponseEntity<TvShowDto> getTvShow(@PathVariable Long tvShowId) throws TvShowsNotFoundException {
        TvShowDto tvShowDto = tvShowService.getTvShow(tvShowId);

        return ResponseEntity.ok(tvShowDto);
    }

    @PostMapping("/{tvShowId}/watchlist/{username}")
    public ResponseEntity<Void> watchlist(@PathVariable("tvShowId") long tvShowId, @PathVariable("username") String username) throws TvShowsNotFoundException {
        tvShowService.addToWatchList(tvShowId, username);

        return ResponseEntity.noContent().build();
    }
}
