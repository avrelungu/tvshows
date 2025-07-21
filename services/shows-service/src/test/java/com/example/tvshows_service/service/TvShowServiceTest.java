package com.example.tvshows_service.service;

import com.example.tvshows_service.dto.TvShowDto;
import com.example.tvshows_service.dto.external.StoreWatchlistDto;
import com.example.tvshows_service.exceptions.TvShowsNotFoundException;
import com.example.tvshows_service.filters.TvShowFilter;
import com.example.tvshows_service.helpers.ReviewHelper;
import com.example.tvshows_service.helpers.WatchlistHelper;
import com.example.tvshows_service.mappers.TvShowMapper;
import com.example.tvshows_service.models.TvShow;
import com.example.tvshows_service.repositories.TvShowRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TvShowServiceTest {

    @Mock
    private TvShowRepository tvShowRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private TvShowMapper tvShowMapper;

    @Mock
    private WatchlistHelper watchlistHelper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReviewHelper reviewHelper;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    
    @Mock 
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private TvShowService tvShowService;

    private TvShow tvShow;
    private TvShowDto tvShowDto;
    private TvShowFilter filter;
    private StoreWatchlistDto storeWatchlistDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tvShowService, "userServiceUrl", "http://user-service");

        tvShow = new TvShow();
        tvShow.setTvShowId(1L);
        tvShow.setName("Test Show");
        tvShow.setRating(8.5);

        tvShowDto = new TvShowDto();
        tvShowDto.setId(1L);
        tvShowDto.setName("Test Show");
        tvShowDto.setRating(8.5);

        filter = new TvShowFilter();
        filter.setName("Test");

        storeWatchlistDto = new StoreWatchlistDto();
        storeWatchlistDto.setTvShowId(1L);
    }

    @Test
    void getTopRatedShows_ShouldReturnShowsSuccessfully() throws TvShowsNotFoundException {
        List<TvShow> shows = Arrays.asList(tvShow);
        Page<TvShow> showsPage = new PageImpl<>(shows);

        when(tvShowRepository.findAll(any(Pageable.class))).thenReturn(showsPage);
        when(tvShowMapper.tvShowToDto(tvShow)).thenReturn(tvShowDto);
        when(watchlistHelper.createAddToWatchlistUrl(1L, "testuser")).thenReturn("http://watchlist-url");

        Page<TvShowDto> result = tvShowService.getTopRatedShows(0, 10, "testuser");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(tvShowDto, result.getContent().get(0));
        verify(tvShowRepository).findAll(any(Pageable.class));
        verify(tvShowMapper).tvShowToDto(tvShow);
        verify(watchlistHelper).createAddToWatchlistUrl(1L, "testuser");
    }

    @Test
    void getTopRatedShows_ShouldThrowExceptionWhenEmpty() {
        Page<TvShow> emptyPage = new PageImpl<>(Arrays.asList());

        when(tvShowRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        assertThrows(TvShowsNotFoundException.class, () -> 
            tvShowService.getTopRatedShows(0, 10, "testuser"));

        verify(tvShowRepository).findAll(any(Pageable.class));
        verifyNoInteractions(tvShowMapper);
        verifyNoInteractions(watchlistHelper);
    }

    @Test
    void getTopRatedShows_ShouldHandleBlankUsername() throws TvShowsNotFoundException {
        List<TvShow> shows = Arrays.asList(tvShow);
        Page<TvShow> showsPage = new PageImpl<>(shows);

        when(tvShowRepository.findAll(any(Pageable.class))).thenReturn(showsPage);
        when(tvShowMapper.tvShowToDto(tvShow)).thenReturn(tvShowDto);

        Page<TvShowDto> result = tvShowService.getTopRatedShows(0, 10, "");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tvShowRepository).findAll(any(Pageable.class));
        verify(tvShowMapper).tvShowToDto(tvShow);
        verifyNoInteractions(watchlistHelper);
    }

    @Test
    void getTvShows_ShouldReturnFilteredShowsSuccessfully() throws TvShowsNotFoundException {
        List<TvShow> shows = Arrays.asList(tvShow);
        Page<TvShow> showsPage = new PageImpl<>(shows);

        when(tvShowRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(showsPage);
        when(tvShowMapper.tvShowToDto(tvShow)).thenReturn(tvShowDto);
        when(watchlistHelper.createAddToWatchlistUrl(1L, "testuser")).thenReturn("http://watchlist-url");
        when(reviewHelper.createReviewUrl(1L, "testuser")).thenReturn("http://review-url");

        Page<TvShowDto> result = tvShowService.getTvShows(0, 10, filter, "testuser");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tvShowRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(tvShowMapper).tvShowToDto(tvShow);
        verify(watchlistHelper).createAddToWatchlistUrl(1L, "testuser");
        verify(reviewHelper).createReviewUrl(1L, "testuser");
    }

    @Test
    void getTvShows_ShouldThrowExceptionWhenNoResults() {
        Page<TvShow> emptyPage = new PageImpl<>(Arrays.asList());

        when(tvShowRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        assertThrows(TvShowsNotFoundException.class, () -> 
            tvShowService.getTvShows(0, 10, filter, "testuser"));

        verify(tvShowRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void addToWatchList_ShouldAddSuccessfully() throws TvShowsNotFoundException {
        when(tvShowRepository.findByTvShowId(1L)).thenReturn(Optional.of(tvShow));
        when(tvShowMapper.tvShowToStoreWatchlistDto(tvShow)).thenReturn(storeWatchlistDto);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));

        tvShowService.addToWatchList(1L, "testuser");

        verify(tvShowRepository).findByTvShowId(1L);
        verify(tvShowMapper).tvShowToStoreWatchlistDto(tvShow);
        verify(webClient).post();
    }

    @Test
    void addToWatchList_ShouldThrowExceptionWhenShowNotFound() {
        when(tvShowRepository.findByTvShowId(1L)).thenReturn(Optional.empty());

        assertThrows(TvShowsNotFoundException.class, () -> 
            tvShowService.addToWatchList(1L, "testuser"));

        verify(tvShowRepository).findByTvShowId(1L);
        verifyNoInteractions(tvShowMapper);
        verifyNoInteractions(webClient);
    }

    @Test
    void getTvShow_ShouldReturnShowSuccessfully() throws TvShowsNotFoundException {
        when(tvShowRepository.findByTvShowId(1L)).thenReturn(Optional.of(tvShow));
        when(tvShowMapper.tvShowToDto(tvShow)).thenReturn(tvShowDto);

        TvShowDto result = tvShowService.getTvShow(1L);

        assertEquals(tvShowDto, result);
        verify(tvShowRepository).findByTvShowId(1L);
        verify(tvShowMapper).tvShowToDto(tvShow);
    }

    @Test
    void getTvShow_ShouldThrowExceptionWhenNotFound() {
        when(tvShowRepository.findByTvShowId(1L)).thenReturn(Optional.empty());

        assertThrows(TvShowsNotFoundException.class, () -> 
            tvShowService.getTvShow(1L));

        verify(tvShowRepository).findByTvShowId(1L);
        verifyNoInteractions(tvShowMapper);
    }

    @Test
    void storeFilteredTvShowsSearchHistory_ShouldStoreSuccessfully() {
        JsonNode filterNode = objectMapper.createObjectNode();
        
        when(objectMapper.valueToTree(filter)).thenReturn(filterNode);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));

        tvShowService.storeFilteredTvShowsSearchHistory("testuser", "/api/shows", filter);

        verify(objectMapper).valueToTree(filter);
        verify(webClient).post();
    }

    @Test
    void getTvShows_ShouldCreateCorrectPageableWithSortDesc() throws TvShowsNotFoundException {
        filter.setSortBy("rating");
        filter.setSortOrder("desc");
        
        List<TvShow> shows = Arrays.asList(tvShow);
        Page<TvShow> showsPage = new PageImpl<>(shows);

        when(tvShowRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(showsPage);
        when(tvShowMapper.tvShowToDto(tvShow)).thenReturn(tvShowDto);
        when(watchlistHelper.createAddToWatchlistUrl(1L, "testuser")).thenReturn("http://watchlist-url");
        when(reviewHelper.createReviewUrl(1L, "testuser")).thenReturn("http://review-url");

        Page<TvShowDto> result = tvShowService.getTvShows(0, 10, filter, "testuser");

        assertNotNull(result);
        verify(tvShowRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTvShows_ShouldCreateCorrectPageableWithSortAsc() throws TvShowsNotFoundException {
        filter.setSortBy("name");
        filter.setSortOrder("asc");
        
        List<TvShow> shows = Arrays.asList(tvShow);
        Page<TvShow> showsPage = new PageImpl<>(shows);

        when(tvShowRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(showsPage);
        when(tvShowMapper.tvShowToDto(tvShow)).thenReturn(tvShowDto);
        when(watchlistHelper.createAddToWatchlistUrl(1L, "testuser")).thenReturn("http://watchlist-url");
        when(reviewHelper.createReviewUrl(1L, "testuser")).thenReturn("http://review-url");

        Page<TvShowDto> result = tvShowService.getTvShows(0, 10, filter, "testuser");

        assertNotNull(result);
        verify(tvShowRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}