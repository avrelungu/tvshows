package com.example.tvshows_service.service;

import com.example.tvshows_service.dto.external.TvMazeShowDto;
import com.example.tvshows_service.mappers.TvShowMapper;
import com.example.tvshows_service.mappers.TvShowMapperImpl;
import com.example.tvshows_service.models.Genre;
import com.example.tvshows_service.models.TvShow;
import com.example.tvshows_service.repositories.GenreRepository;
import com.example.tvshows_service.repositories.TvShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeederServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private TvShowRepository tvShowRepository;

    @Mock
    private TvShowMapper tvShowMapper;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private TvShowMapperImpl tvShowMapperImpl;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private SeederService seederService;

    private TvMazeShowDto tvMazeShowDto;
    private TvShow tvShow;
    private Genre genre;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(seederService, "tvMazeAPIUrl", "https://api.tvmaze.com");

        tvMazeShowDto = new TvMazeShowDto();
        tvMazeShowDto.setId(1L);
        tvMazeShowDto.setName("Test Show");
        tvMazeShowDto.setGenres(Arrays.asList("Drama", "Comedy"));

        tvShow = new TvShow();
        tvShow.setTvShowId(1L);
        tvShow.setName("Test Show");

        genre = new Genre();
        genre.setName("Drama");
    }

    @Test
    void fetchAllTvShows_ShouldReturnShowsSuccessfully() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(TvMazeShowDto.class))
                .thenReturn(Flux.just(tvMazeShowDto));

        List<TvMazeShowDto> result = seederService.fetchAllTvShows();

        assertNotNull(result);
        verify(webClient, times(8)).get();
    }

    @Test
    void fetchAllTvShows_ShouldHandleErrorsGracefully() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(TvMazeShowDto.class))
                .thenReturn(Flux.error(new RuntimeException("Service unavailable")));

        List<TvMazeShowDto> result = seederService.fetchAllTvShows();

        assertNotNull(result);
        verify(webClient, times(8)).get();
    }

    @Test
    void saveTvShows_ShouldSaveSuccessfully() {
        List<TvMazeShowDto> shows = Collections.singletonList(tvMazeShowDto);
        
        Genre existingGenre = new Genre();
        existingGenre.setName("Drama");
        Genre newGenre = new Genre();
        newGenre.setName("Comedy");
        
        when(genreRepository.findByNameIn(any())).thenReturn(List.of(existingGenre));
        when(genreRepository.saveAll(any())).thenReturn(List.of(newGenre));
        when(tvShowMapper.mazeDtoToTvShow(any(TvMazeShowDto.class))).thenReturn(tvShow);
        when(tvShowRepository.saveAll(any())).thenReturn(Collections.singletonList(tvShow));

        seederService.saveTvShows(shows);

        verify(genreRepository).findByNameIn(any());
        verify(genreRepository).saveAll(any());
    }

    @Test
    void saveTvShows_ShouldHandleEmptyList() {
        List<TvMazeShowDto> emptyShows = new ArrayList<>();

        seederService.saveTvShows(emptyShows);

        verifyNoInteractions(genreRepository);
        verifyNoInteractions(tvShowMapper);
        verifyNoInteractions(tvShowRepository);
    }

    @Test
    void saveTvShows_ShouldHandleDataIntegrityViolation() {
        List<TvMazeShowDto> shows = List.of(tvMazeShowDto);
        
        when(genreRepository.findByNameIn(any())).thenReturn(new ArrayList<>());
        when(genreRepository.saveAll(any())).thenReturn(List.of(genre));
        when(tvShowMapper.mazeDtoToTvShow(any(TvMazeShowDto.class))).thenReturn(tvShow);
        when(tvShowRepository.saveAll(any())).thenThrow(new DataIntegrityViolationException("Constraint violation"));
        when(tvShowRepository.save(any(TvShow.class))).thenReturn(tvShow);

        seederService.saveTvShows(shows);

        verify(genreRepository).findByNameIn(any());
        verify(genreRepository).saveAll(any());
        verify(tvShowMapper).mazeDtoToTvShow(any(TvMazeShowDto.class));
        verify(tvShowRepository).saveAll(any());
        verify(tvShowRepository).save(any(TvShow.class));
    }

    @Test
    void saveTvShows_ShouldHandleMappingFailure() {
        TvMazeShowDto failingShow = new TvMazeShowDto();
        failingShow.setId(999L);
        failingShow.setName("Failing Show");
        failingShow.setGenres(List.of("Drama"));
        
        List<TvMazeShowDto> shows = List.of(failingShow);
        
        when(genreRepository.findByNameIn(any())).thenReturn(List.of(genre));
        when(tvShowMapper.mazeDtoToTvShow(any(TvMazeShowDto.class))).thenReturn(null);
        when(tvShowRepository.saveAll(any())).thenReturn(new ArrayList<>());

        seederService.saveTvShows(shows);

        verify(genreRepository).findByNameIn(any());
    }

    @Test
    void saveTvShows_ShouldCreateNewGenres() {
        TvMazeShowDto showWithNewGenre = new TvMazeShowDto();
        showWithNewGenre.setId(1L);
        showWithNewGenre.setGenres(List.of("NewGenre"));
        
        List<TvMazeShowDto> shows = List.of(showWithNewGenre);
        
        Genre newGenre = new Genre();
        newGenre.setName("NewGenre");
        
        when(genreRepository.findByNameIn(any())).thenReturn(new ArrayList<>());
        when(genreRepository.saveAll(any())).thenReturn(List.of(newGenre));
        when(tvShowMapper.mazeDtoToTvShow(any(TvMazeShowDto.class))).thenReturn(tvShow);
        when(tvShowRepository.saveAll(any())).thenReturn(List.of(tvShow));

        seederService.saveTvShows(shows);

        verify(genreRepository).findByNameIn(any());
        verify(genreRepository).saveAll(any());
    }

    @Test
    void saveTvShows_ShouldHandleShowsWithoutGenres() {
        TvMazeShowDto showWithoutGenres = new TvMazeShowDto();
        showWithoutGenres.setId(1L);
        showWithoutGenres.setGenres(null);
        
        List<TvMazeShowDto> shows = List.of(showWithoutGenres);
        
        when(tvShowMapper.mazeDtoToTvShow(any(TvMazeShowDto.class))).thenReturn(tvShow);
        when(tvShowRepository.saveAll(any())).thenReturn(List.of(tvShow));

        seederService.saveTvShows(shows);

        verify(tvShowRepository).saveAll(any());
    }
}