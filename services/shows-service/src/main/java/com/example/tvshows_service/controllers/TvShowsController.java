package com.example.tvshows_service.controllers;

import com.example.tvshows_service.assembler.TvShowModelAssembler;
import com.example.tvshows_service.dto.TvShowDto;
import com.example.tvshows_service.exceptions.TvShowsNotFoundException;
import com.example.tvshows_service.filters.TvShowFilter;
import com.example.tvshows_service.models.TvShow;
import com.example.tvshows_service.service.TvMazeSyncService;
import com.example.tvshows_service.service.TvShowService;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tv-shows")
public class TvShowsController {
    private final TvShowService tvShowService;
    private final TvMazeSyncService tvMazeSyncService;
    private final TvShowModelAssembler tvShowModelAssembler;

    public TvShowsController(
            TvShowService tvShowService,
            TvMazeSyncService tvMazeSyncService,
            TvShowModelAssembler tvShowModelAssembler) {
        this.tvShowService = tvShowService;
        this.tvMazeSyncService = tvMazeSyncService;
        this.tvShowModelAssembler = tvShowModelAssembler;
    }

    @GetMapping
    public ResponseEntity<PagedModel<TvShowDto>> getTvShows(
            @ModelAttribute TvShowFilter tvShowFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<TvShow> pagedResourcesAssembler
    ) throws TvShowsNotFoundException {
        Page<TvShow> tvShowPage = tvShowService.getTvShows(page, size, tvShowFilter);

        PagedModel<TvShowDto> tvShows = pagedResourcesAssembler.toModel(tvShowPage, tvShowModelAssembler);

        return ResponseEntity.ok(tvShows);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<PagedModel<TvShowDto>> topRatedShows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<TvShow> pagedResourcesAssembler
    ) throws TvShowsNotFoundException {
        Page<TvShow> showList = tvShowService.getTopRatedShows(page, size);

        PagedModel<TvShowDto> tvShowDtoPagedModel = pagedResourcesAssembler.toModel(showList, tvShowModelAssembler);

        return ResponseEntity.ok(tvShowDtoPagedModel);
    }

    @GetMapping("/sync")
    public ResponseEntity<String> syncTvShows() {
        CompletableFuture.runAsync(tvMazeSyncService::syncTvShows);

        return ResponseEntity.ok("Sync started to run");
    }
}
