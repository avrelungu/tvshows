package com.example.tvshows_service.service;

import com.example.tvshows_service.dto.TvShowDto;
import com.example.tvshows_service.dto.external.StoreTvShowSearchDto;
import com.example.tvshows_service.dto.external.StoreWatchlistDto;
import com.example.tvshows_service.dto.external.TvMazeShowDto;
import com.example.tvshows_service.exceptions.TvShowsNotFoundException;
import com.example.tvshows_service.filters.TvShowFilter;
import com.example.tvshows_service.helpers.ReviewHelper;
import com.example.tvshows_service.helpers.WatchlistHelper;
import com.example.tvshows_service.mappers.TvShowMapper;
import com.example.tvshows_service.models.TvShow;
import com.example.tvshows_service.repositories.TvShowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TvShowService {
    @Value("${user-service.api.url}")
    private String userServiceUrl;

    @Value("${tv-maze.api.size}")
    private int EXTERNAL_API_PAGE_SIZE;

    @Value("${tv-maze.api.url}")
    private String tvMazeApiUrl;

    private final WebClient webClient;

    private final TvShowMapper tvShowMapper;

    private final WatchlistHelper watchlistHelper;

    private final ObjectMapper objectMapper;

    private final ReviewHelper reviewHelper;

    private final RedisTemplate<String, TvShowDto> redisTemplate;

    private final TvShowRepository tvShowRepository;

    public TvShowService(
            TvShowRepository tvShowRepository,
            WebClient webClient,
            TvShowMapper tvShowMapper,
            WatchlistHelper watchlistHelper,
            ObjectMapper objectMapper,
            ReviewHelper reviewHelper,
            RedisTemplate<String, TvShowDto> tvShowsRedisTemplate
    ) {
        this.tvShowRepository = tvShowRepository;
        this.webClient = webClient;
        this.tvShowMapper = tvShowMapper;
        this.watchlistHelper = watchlistHelper;
        this.objectMapper = objectMapper;
        this.reviewHelper = reviewHelper;
        this.redisTemplate = tvShowsRedisTemplate;
    }

    @Cacheable(value = "topRatedShows")
    public Page<TvShowDto> getTopRatedShows(int page, int size, String username) throws TvShowsNotFoundException {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        Page<TvShowDto> tvShowsPage = tvShowRepository.findAll(pageable)
                .map(tvShow -> {
                            TvShowDto tvShowDto = tvShowMapper.tvShowToDto(tvShow);
                            addWatchListUrl(tvShowDto, username);

                            return tvShowDto;
                        }
                );

        if (tvShowsPage.isEmpty()) {
            throw new TvShowsNotFoundException();
        }

        return tvShowsPage;
    }

    public Page<TvShowDto> getTvShows(int page, int size, TvShowFilter filter, String username, String url) throws TvShowsNotFoundException {
        int fromIndex = page * size;
        int toIndex = fromIndex + size;

        String cacheKey = "tvshows:page";

        List<TvShowDto> cachedShows = redisTemplate.opsForList().range(cacheKey, fromIndex, toIndex - 1);

        if (cachedShows == null || cachedShows.size() < size) {
            long cachedSize = redisTemplate.opsForList().size(cacheKey);

            if (cachedSize < toIndex) {
                int startExternalPage = Math.toIntExact(cachedSize / EXTERNAL_API_PAGE_SIZE);
                int endExternalPAge = Math.toIntExact((toIndex - 1) / EXTERNAL_API_PAGE_SIZE);

                for (int externalPage = startExternalPage; externalPage <= endExternalPAge; externalPage++) {
                    List<TvShowDto> externalPageShows = fetchExternalPage(externalPage, username);

                    if (!externalPageShows.isEmpty()) {
                        redisTemplate.opsForList().rightPushAll(cacheKey, externalPageShows);
                        ;
                    }
                }

                cachedShows = redisTemplate.opsForList().range(cacheKey, fromIndex, toIndex - 1);
            }
        }

        if (cachedShows == null || cachedShows.isEmpty()) {
            throw new TvShowsNotFoundException();
        }

        Pageable pageable = createPageable(page, size, filter.getSortBy(), filter.getSortOrder());
        long totalElements = Optional.ofNullable(redisTemplate.opsForList().size(cacheKey)).orElse(0L);

        StoreTvShowSearchDto storeTvShowSearchDto = new StoreTvShowSearchDto();
        storeTvShowSearchDto.setEndpoint(url);
        storeTvShowSearchDto.setFilters(objectMapper.valueToTree(filter));

        webClient.post()
                .uri(userServiceUrl + "/api/users/search/store/" + username)
                .bodyValue(storeTvShowSearchDto)
                .retrieve()
                .toBodilessEntity()
                .block();

        return new PageImpl<>(cachedShows, pageable, totalElements);

//        Specification<TvShow> spec = Stream.of(
//                        TvShowSpecification.hasName(filter.getName()),
//                        TvShowSpecification.hasDescription(filter.getDescription()),
//                        TvShowSpecification.hasNetwork(filter.getNetwork()),
//                        TvShowSpecification.hasStatus(filter.getStatus()),
//                        TvShowSpecification.endedBefore(filter.getEnded()),
//                        TvShowSpecification.premieredAfter(filter.getPremiered()),
//                        TvShowSpecification.hasLanguage(filter.getLanguage()),
//                        TvShowSpecification.ratingBetween(filter.getMinRating(), filter.getMaxRating()),
//                        TvShowSpecification.hasGenres(filter.getGenres())
//                )
//                .filter(Objects::nonNull)
//                .reduce(Specification::and)
//                .orElse(null);
//
//        Page<TvShowDto> tvShowPage = tvShowRepository.findAll(spec, pageable)
//                .map(tvShow -> addWatchListUrl(tvShow, username))
//                .map(tvShowDto -> addReviewUrl(tvShowDto, username));
//
//        if (tvShowPage.isEmpty()) {
//            throw new TvShowsNotFoundException();
//        }
    }

    private List<TvShowDto> fetchExternalPage(int externalPage, String username) {
        String tvMazeApiUrl = this.tvMazeApiUrl + "/shows?page=" + externalPage;

        try {

            return webClient.get()
                    .uri(tvMazeApiUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("Error while fetching external page: {}", clientResponse.statusCode());
                        return clientResponse.createException();
                    })
                    .bodyToFlux(TvMazeShowDto.class)
                    .map(tvShowMapper::mazeDtoToTvShowDto)
                    .map(tvShowDto -> addReviewUrl(addWatchListUrl(tvShowDto, username), username))
                    .collectList()
                    .block();

        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException fetching page {}: {}", externalPage, e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Exception fetching external page {}: {}", externalPage, e.getMessage());
            return List.of();
        }
    }

    public void addToWatchList(long tvShowId, String username) throws TvShowsNotFoundException {
        TvShow tvShow = tvShowRepository.findByTvShowId(tvShowId).orElseThrow(TvShowsNotFoundException::new);

        StoreWatchlistDto storeWatchlistDto = tvShowMapper.tvShowToStoreWatchlistDto(tvShow);

        webClient.post()
                .uri(userServiceUrl + "/api/users/watchlist/" + username)
                .bodyValue(storeWatchlistDto)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private TvShowDto addWatchListUrl(TvShowDto tvShowDto, String username) {
        if (username.isBlank()) {
            return tvShowDto;
        }

        tvShowDto.setWatchlistUrl(watchlistHelper.createAddToWatchlistUrl(tvShowDto.getId(), username));

        return tvShowDto;
    }

    private TvShowDto addReviewUrl(TvShowDto tvShowDto, String username) {
        tvShowDto.setReviewUrl(reviewHelper.createReviewUrl(tvShowDto.getId(), username));

        return tvShowDto;
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortOrder) {
        Sort sort = Sort.unsorted();

        if (sortBy != null && !sortBy.isBlank()) {
            sort = Sort.by(sortBy);
            if ("desc".equalsIgnoreCase(sortOrder)) {
                sort = sort.descending();
            } else {
                sort = sort.ascending();
            }
        }

        return PageRequest.of(page, size, sort);
    }
}
