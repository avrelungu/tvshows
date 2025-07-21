package com.example.user_service.services;

import com.example.user_service.dto.StoreSearchHistoryUserEvent;
import com.example.user_service.models.UserProfile;
import com.example.user_service.repositories.TvShowSearchHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TvShowSearchHistoryCleanUpServiceTest {

    @Mock
    private TvShowSearchHistoryRepository tvShowSearchHistoryRepository;

    @InjectMocks
    private TvShowSearchHistoryCleanUpService tvShowSearchHistoryCleanUpService;

    private UserProfile userProfile;
    private StoreSearchHistoryUserEvent event;

    @BeforeEach
    void setUp() {
        userProfile = new UserProfile();
        userProfile.setUsername("testuser");
        event = new StoreSearchHistoryUserEvent(userProfile);
    }

    @Test
    void cleanUpSearchHistory_ShouldCallRepositoryCleanUp() {
        tvShowSearchHistoryCleanUpService.cleanUpSearchHistory(event);

        verify(tvShowSearchHistoryRepository).cleanUpSearchHistory(userProfile);
    }

    @Test
    void cleanUpSearchHistory_ShouldHandleException() {
        doThrow(new RuntimeException("Database error")).when(tvShowSearchHistoryRepository)
                .cleanUpSearchHistory(userProfile);

        tvShowSearchHistoryCleanUpService.cleanUpSearchHistory(event);

        verify(tvShowSearchHistoryRepository).cleanUpSearchHistory(userProfile);
    }
}