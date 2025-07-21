package com.example.user_service.services;

import com.example.user_service.dto.StoreSearchHistoryUserEvent;
import com.example.user_service.dto.StoreShowsSearchDto;
import com.example.user_service.dto.TvShowSearchHistoryDto;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.mappers.TvShowSearchHistoryMapper;
import com.example.user_service.models.TvShowSearchHistory;
import com.example.user_service.models.UserProfile;
import com.example.user_service.repositories.TvShowSearchHistoryRepository;
import com.example.user_service.repositories.UserProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TvShowSearchServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private TvShowSearchHistoryRepository tvShowSearchHistoryRepository;

    @Mock
    private TvShowSearchHistoryMapper tvShowSearchHistoryMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TvShowSearchService tvShowSearchService;

    private UserProfile userProfile;
    private StoreShowsSearchDto storeShowsSearchDto;
    private TvShowSearchHistory tvShowSearchHistory;
    private TvShowSearchHistoryDto tvShowSearchHistoryDto;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userProfile = new UserProfile();
        userProfile.setUsername("testuser");

        JsonNode filtersNode = objectMapper.createObjectNode().put("genre", "comedy");
        
        storeShowsSearchDto = new StoreShowsSearchDto();
        storeShowsSearchDto.setSearchTime(LocalDateTime.now());
        storeShowsSearchDto.setFilters(filtersNode);
        storeShowsSearchDto.setEndpoint("/api/shows");

        tvShowSearchHistory = new TvShowSearchHistory();
        tvShowSearchHistory.setUserProfile(userProfile);
        tvShowSearchHistory.setSearchTime(storeShowsSearchDto.getSearchTime());
        tvShowSearchHistory.setFilters(storeShowsSearchDto.getFilters());
        tvShowSearchHistory.setEndpoint(storeShowsSearchDto.getEndpoint());

        tvShowSearchHistoryDto = new TvShowSearchHistoryDto();
    }

    @Test
    void storeTvShowSearch_ShouldStoreSearchSuccessfully() throws UserProfileNotFoundException {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));
        when(tvShowSearchHistoryRepository.findByUserProfileOrderBySearchTimeDesc(userProfile))
                .thenReturn(Arrays.asList(tvShowSearchHistory));

        tvShowSearchService.storeTvShowSearch("testuser", storeShowsSearchDto);

        verify(userProfileRepository).findByUsername("testuser");
        verify(tvShowSearchHistoryRepository).save(any(TvShowSearchHistory.class));
        verify(tvShowSearchHistoryRepository).findByUserProfileOrderBySearchTimeDesc(userProfile);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void storeTvShowSearch_ShouldThrowExceptionWhenUserNotFound() {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class, () -> 
            tvShowSearchService.storeTvShowSearch("testuser", storeShowsSearchDto));

        verify(userProfileRepository).findByUsername("testuser");
        verifyNoInteractions(tvShowSearchHistoryRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void storeTvShowSearch_ShouldPublishEventWhenHistoryExceedsLimit() throws UserProfileNotFoundException {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));
        
        List<TvShowSearchHistory> largeHistoryList = Arrays.asList(
            new TvShowSearchHistory(), new TvShowSearchHistory(), new TvShowSearchHistory(),
            new TvShowSearchHistory(), new TvShowSearchHistory(), new TvShowSearchHistory(),
            new TvShowSearchHistory(), new TvShowSearchHistory(), new TvShowSearchHistory(),
            new TvShowSearchHistory(), new TvShowSearchHistory()
        );
        
        when(tvShowSearchHistoryRepository.findByUserProfileOrderBySearchTimeDesc(userProfile))
                .thenReturn(largeHistoryList);

        tvShowSearchService.storeTvShowSearch("testuser", storeShowsSearchDto);

        verify(userProfileRepository).findByUsername("testuser");
        verify(tvShowSearchHistoryRepository).save(any(TvShowSearchHistory.class));
        verify(tvShowSearchHistoryRepository).findByUserProfileOrderBySearchTimeDesc(userProfile);
        verify(applicationEventPublisher).publishEvent(any(StoreSearchHistoryUserEvent.class));
    }

    @Test
    void getSearchHistory_ShouldReturnHistorySuccessfully() throws UserProfileNotFoundException {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));
        when(tvShowSearchHistoryRepository.findByUserProfileOrderBySearchTimeDesc(userProfile))
                .thenReturn(Arrays.asList(tvShowSearchHistory));
        when(tvShowSearchHistoryMapper.toDto(tvShowSearchHistory)).thenReturn(tvShowSearchHistoryDto);

        List<TvShowSearchHistoryDto> result = tvShowSearchService.getSearchHistory("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tvShowSearchHistoryDto, result.get(0));

        verify(userProfileRepository).findByUsername("testuser");
        verify(tvShowSearchHistoryRepository).findByUserProfileOrderBySearchTimeDesc(userProfile);
        verify(tvShowSearchHistoryMapper).toDto(tvShowSearchHistory);
    }

    @Test
    void getSearchHistory_ShouldThrowExceptionWhenUserNotFound() {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class, () -> 
            tvShowSearchService.getSearchHistory("testuser"));

        verify(userProfileRepository).findByUsername("testuser");
        verifyNoInteractions(tvShowSearchHistoryRepository);
        verifyNoInteractions(tvShowSearchHistoryMapper);
    }
}