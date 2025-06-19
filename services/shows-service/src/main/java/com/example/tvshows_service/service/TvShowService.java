package com.example.tvshows_service.service;

import com.example.tvshows_service.dto.TvShowDto;
import com.example.tvshows_service.dto.external.StoreWatchlistDto;
import com.example.tvshows_service.exceptions.TvShowsNotFoundException;
import com.example.tvshows_service.filters.TvShowFilter;
import com.example.tvshows_service.helpers.WatchlistHelper;
import com.example.tvshows_service.mappers.TvShowMapper;
import com.example.tvshows_service.models.TvShow;
import com.example.tvshows_service.repositories.TvShowRepository;
import com.example.tvshows_service.specifications.TvShowSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.stream.Stream;

@Service
@Slf4j
public class TvShowService {
    private final WebClient webClient;

    private final TvShowMapper tvShowMapper;
    private final WatchlistHelper watchlistHelper;

    @Value("${user-service.api.url}")
    private String userServiceUrl;

    private final TvShowRepository tvShowRepository;

    public TvShowService(
            TvShowRepository tvShowRepository,
            WebClient webClient, TvShowMapper tvShowMapper, WatchlistHelper watchlistHelper) {
        this.tvShowRepository = tvShowRepository;
        this.webClient = webClient;
        this.tvShowMapper = tvShowMapper;
        this.watchlistHelper = watchlistHelper;
    }

    public Page<TvShowDto> getTopRatedShows(int page, int size, String username) throws TvShowsNotFoundException {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        Page<TvShowDto> tvShowsPage = tvShowRepository.findAll(pageable)
                .map(tvShow -> addWatchListUrl(tvShow, username));

        if (tvShowsPage.isEmpty()) {
            throw new TvShowsNotFoundException();
        }

        return tvShowsPage;
    }

    public Page<TvShowDto> getTvShows(int page, int size, TvShowFilter filter, String username) throws TvShowsNotFoundException {
        Pageable pageable = createPageable(page, size, filter.getSortBy(), filter.getSortOrder());

        Specification<TvShow> spec = Stream.of(
                        TvShowSpecification.hasName(filter.getName()),
                        TvShowSpecification.hasDescription(filter.getDescription()),
                        TvShowSpecification.hasNetwork(filter.getNetwork()),
                        TvShowSpecification.hasStatus(filter.getStatus()),
                        TvShowSpecification.endedBefore(filter.getEnded()),
                        TvShowSpecification.premieredAfter(filter.getPremiered()),
                        TvShowSpecification.hasLanguage(filter.getLanguage()),
                        TvShowSpecification.ratingBetween(filter.getMinRating(), filter.getMaxRating()),
                        TvShowSpecification.hasGenres(filter.getGenres())
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(null);

        Page<TvShowDto> tvShowPage = tvShowRepository.findAll(spec, pageable)
                .map(tvShow -> addWatchListUrl(tvShow, username));

        if (tvShowPage.isEmpty()) {
            throw new TvShowsNotFoundException();
        }

        return tvShowPage;
    }

    public void addToWatchList(long tvShowId, String username) throws TvShowsNotFoundException {
        TvShow tvShow = tvShowRepository.findByTvShowId(tvShowId).orElseThrow(TvShowsNotFoundException::new);

        StoreWatchlistDto storeWatchlistDto = tvShowMapper.tvShowToStoreWatchlistDto(tvShow);

        log.info(storeWatchlistDto.toString());

        webClient.post()
                .uri(userServiceUrl + "/api/users/watchlist/" + username)
                .bodyValue(storeWatchlistDto)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private TvShowDto addWatchListUrl(TvShow tvShow, String username) {
        TvShowDto tvShowDto = tvShowMapper.tvShowToDto(tvShow);

        if (username.isBlank()) {
            return tvShowDto;
        }

        tvShowDto.setWatchlistUrl(watchlistHelper.createAddToWatchlistUrl(tvShowDto.getId(), username));

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
