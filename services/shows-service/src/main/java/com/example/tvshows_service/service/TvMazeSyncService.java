package com.example.tvshows_service.service;

import com.example.tvshows_service.dto.external.TvMazeShowDto;
import com.example.tvshows_service.mappers.TvShowMapper;
import com.example.tvshows_service.models.Genre;
import com.example.tvshows_service.models.TvShow;
import com.example.tvshows_service.repositories.GenreRepository;
import com.example.tvshows_service.repositories.TvShowRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TvMazeSyncService {

    private final TvShowRepository tvShowRepository;
    private final GenreRepository genreRepository;
    private final TvShowMapper tvShowMapper;

    @Value("${tv-maze.api.url}")
    private String tvMazeUrl;

    private WebClient webClient;

    public TvMazeSyncService(WebClient webClient,
                             TvShowRepository tvShowRepository,
                             GenreRepository genreRepository,
                             TvShowMapper tvShowMapper) {
        this.webClient = webClient;
        this.tvShowRepository = tvShowRepository;
        this.genreRepository = genreRepository;
        this.tvShowMapper = tvShowMapper;
    }

    @Transactional
    @Scheduled(cron = "0 0 9 * * Mon")
    public void syncTvShows() {
        log.info("Syncing tv shows");

        int page = 0;
        int totalShows = 0;
        boolean hasMorePages = true;

        while (hasMorePages) {
            try {
                List<TvMazeShowDto> tvShows = fetchTvShowsPage(page);
                log.info("Found {} tv shows", tvShows.size());

                if (tvShows.isEmpty()) {
                    log.info("No tv shows found");
                    hasMorePages = false;
                    log.info("No TV Shows left to sync");
                } else {
                    List<Long> tvShowIds = tvShows.stream()
                            .map(TvMazeShowDto::getId)
                            .toList();

                    Optional<List<TvShow>> existingShowsOpt = tvShowRepository.findAllByTvShowIdIn(tvShowIds);

                    Map<Long, TvShow> existingShows = existingShowsOpt
                            .map(list -> list.stream()
                                    .collect(Collectors.toMap(TvShow::getTvShowId, Function.identity())))
                            .orElse(Collections.emptyMap());

                    // Cache all genres from DB to minimize DB hits per show
                    Map<String, Genre> existingGenres = genreRepository.findAll().stream()
                            .collect(Collectors.toMap(
                                    g -> g.getName().toLowerCase(),
                                    Function.identity()
                            ));

                    List<TvShow> showsToSave = tvShows.stream()
                            .map(dto -> {
                                TvShow tvShow = existingShows.get(dto.getId());
                                if (tvShow != null) {
                                    tvShowMapper.updateTvShowFromMazeTvShowDto(dto, tvShow);
                                } else {
                                    tvShow = tvShowMapper.mazeDtoToTvShow(dto);
                                }

                                // Process genres: convert genre names from dto to Set<Genre> entities
                                Set<Genre> genres = dto.getGenres() == null ? Collections.emptySet() :
                                        dto.getGenres().stream()
                                                .map(String::toLowerCase)
                                                .map(name -> {
                                                    // Lookup existing genre or create new one
                                                    Genre genre = existingGenres.get(name);
                                                    if (genre == null) {
                                                        genre = new Genre();
                                                        genre.setName(name);
                                                        genre = genreRepository.save(genre);
                                                        existingGenres.put(name, genre);
                                                    }
                                                    return genre;
                                                })
                                                .collect(Collectors.toSet());

                                tvShow.setGenres(genres);

                                return tvShow;
                            })
                            .toList();

                    tvShowRepository.saveAll(showsToSave);
                }

                page++;
                totalShows += tvShows.size();

                Thread.sleep(500);
            } catch (Exception e) {
                log.error("TV Shows sync failed: {}", e.getMessage());
                break;
            }
        }

        log.info("TV Shows sync completed: {} TV shows synced", totalShows);
    }

    private List<TvMazeShowDto> fetchTvShowsPage(int page) {
        log.info("Fetching tv shows page {}", page);

        String uri = String.format(tvMazeUrl + "/shows?page=%d", page);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(TvMazeShowDto.class)
                .collectList()
                .block();
    }
}
