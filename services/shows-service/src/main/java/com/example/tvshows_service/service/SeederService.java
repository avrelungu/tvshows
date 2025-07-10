package com.example.tvshows_service.service;

import com.example.tvshows_service.dto.external.TvMazeShowDto;
import com.example.tvshows_service.mappers.TvShowMapper;
import com.example.tvshows_service.mappers.TvShowMapperImpl;
import com.example.tvshows_service.models.Genre;
import com.example.tvshows_service.models.TvShow;
import com.example.tvshows_service.repositories.GenreRepository;
import com.example.tvshows_service.repositories.TvShowRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeederService {

    private final TvShowMapperImpl tvShowMapperImpl;
    @Value("${tv-maze.api.url}")
    private String tvMazeAPIUrl;

    private static final int PAGES_TO_FETCH = 8;
    private static final int BATCH_SIZE = 50;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    private final WebClient webClient;
    private final TvShowRepository tvShowRepository;
    private final TvShowMapper tvShowMapper;
    private final GenreRepository genreRepository;

    public SeederService(
            WebClient webClient,
            TvShowRepository tvShowRepository,
            TvShowMapper tvShowMapper,
            GenreRepository genreRepository,
            TvShowMapperImpl tvShowMapperImpl) {
        this.webClient = webClient;
        this.tvShowRepository = tvShowRepository;
        this.tvShowMapper = tvShowMapper;
        this.genreRepository = genreRepository;
        this.tvShowMapperImpl = tvShowMapperImpl;
    }

    private Mono<List<TvMazeShowDto>> fetchTvShowsPage(int page) {
        return webClient.get()
                .uri(tvMazeAPIUrl + "/shows?page=" + page)
                .retrieve()
                .bodyToFlux(TvMazeShowDto.class)
                .timeout(REQUEST_TIMEOUT)
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_DELAY)
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable || throwable instanceof WebClientResponseException.GatewayTimeout)
                        .doBeforeRetry(retrySignal -> log.warn("Retrying page {} due to error: {}", page, retrySignal.failure().getMessage()))
                )
                .collectList()
                .onErrorResume(error -> {
                    log.error("Failed to fetch page {} after retries: {}", page, error.getMessage());
                    return Mono.just(new ArrayList<>());
                });
    }

    public List<TvMazeShowDto> fetchAllTvShows() {
        AtomicInteger totalShows = new AtomicInteger(0);

        List<TvMazeShowDto> allShows = Flux.range(0, PAGES_TO_FETCH)
                .flatMap(page -> fetchTvShowsPage(page)
                        .doOnNext(shows -> {
                            int count = shows.size();
                            totalShows.addAndGet(count);
                            log.info("Progress: fetched {} shows from page {}/{}", shows.size(), page + 1, PAGES_TO_FETCH);
                        }), 4)
                .collectList()
                .map(lists -> lists.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList())
                )
                .block();

        log.info("{} Total shows fetched from {} pages", totalShows.get(), PAGES_TO_FETCH);

        return allShows != null ? allShows : new ArrayList<>();
    }

    @Transactional
    public void saveTvShows(List<TvMazeShowDto> tvShows) {
        if (tvShows.isEmpty()) {
            log.warn("No tv shows to save");
            return;
        }
        
        Map<String, Genre> genreCache = createOrGetGenres(tvShows);
        
        List<TvShow> tvShowEntities = new ArrayList<>(tvShows.size());
        int skippedShows = 0;
        
        for (TvMazeShowDto tvShowDto : tvShows) {
            try {
                TvShow tvShow = tvShowMapper.mazeDtoToTvShow(tvShowDto);
                
                if (tvShowDto.getGenres() != null && !tvShowDto.getGenres().isEmpty()) {
                    Set<Genre> genres = tvShowDto.getGenres().stream()
                            .map(genreCache::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    
                    tvShow.setGenres(genres);
                }

                tvShowEntities.add(tvShow);
            } catch (Exception e) {
                log.error("Failed to convert TV Show with id {}: {}", tvShowDto.getId(), e.getMessage());
                skippedShows++;
            }
        }

        List<TvShow> savedShows = saveInBatches(tvShowEntities);

        log.info("Saved {} tv shows", savedShows.size());
    }

    private List<TvShow> saveInBatches(List<TvShow> tvShows) {
        List<TvShow> allSavedShows = new ArrayList<>();

        for (int i = 0; i < tvShows.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, tvShows.size());
            List<TvShow> batch = tvShows.subList(i, end);

            try {
                List<TvShow> savedBatch = tvShowRepository.saveAll(batch);
                allSavedShows.addAll(savedBatch);

                log.debug("Saved batch {}/{} ({} shows)",
                        (i / BATCH_SIZE) + 1,
                        (tvShows.size() + BATCH_SIZE - 1) / BATCH_SIZE,
                        savedBatch.size());
            } catch (DataIntegrityViolationException e) {
                log.error("Failed to save batch starting at index {}. Attempting individual saves.", i);

                for (TvShow show : batch) {
                    try {
                        TvShow saved = tvShowRepository.save(show);
                        allSavedShows.add(saved);
                    } catch (Exception individualError) {
                        log.error("Failed to save show with tvShowId {}: {}",
                                show.getTvShowId(), individualError.getMessage());
                    }
                }
            }
        }

        return allSavedShows;
    }

    private Map<String, Genre> createOrGetGenres(List<TvMazeShowDto> tvShows) {
        Set<String> genreNames = tvShows.stream()
                .filter(show -> show.getGenres() != null)
                .flatMap(show -> show.getGenres().stream())
                .collect(Collectors.toSet());

        log.debug("Found {} unique genres", genreNames.size());

        Map<String, Genre> genreMap = new HashMap<>();
        List<Genre> existingGenres = genreRepository.findByNameIn(new ArrayList<>(genreNames));
        existingGenres.forEach(genre -> genreMap.put(genre.getName(), genre));

        Set<String> existingGenreNames = existingGenres.stream()
                .map(Genre::getName)
                .collect(Collectors.toSet());

        List<Genre> newGenres = genreNames.stream()
                .filter(name -> !existingGenreNames.contains(name))
                .map(name -> {
                    Genre genre = new Genre();
                    genre.setName(name);
                    return genre;
                })
                .collect(Collectors.toList());

        if (!newGenres.isEmpty()) {
            log.info("Creating {} new genres", newGenres.size());
            List<Genre> savedGenres = genreRepository.saveAll(newGenres);
            savedGenres.forEach(genre -> genreMap.put(genre.getName(), genre));
        }

        return genreMap;
    }
}
