package com.example.user_service.services;

import com.example.user_service.dto.StoreWatchlistDto;
import com.example.user_service.dto.WatchlistDto;
import com.example.user_service.exceptions.AppException;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.mappers.WatchlistMapper;
import com.example.user_service.models.UserProfile;
import com.example.user_service.models.Watchlist;
import com.example.user_service.repositories.UserProfileRepository;
import com.example.user_service.repositories.WatchlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private WatchlistMapper watchlistMapper;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private WatchlistService watchlistService;

    private UserProfile userProfile;
    private Watchlist watchlist;
    private WatchlistDto watchlistDto;
    private StoreWatchlistDto storeWatchlistDto;

    @BeforeEach
    void setUp() {
        userProfile = new UserProfile();
        userProfile.setUsername("testuser");
        userProfile.setWatchlists(new ArrayList<>());

        watchlist = new Watchlist();
        watchlist.setShowId(123);
        watchlist.setUserProfile(userProfile);

        watchlistDto = new WatchlistDto();
        watchlistDto.setShowId(123);

        storeWatchlistDto = new StoreWatchlistDto();
        storeWatchlistDto.setTvShowId(123L);
    }

    @Test
    void getWatchlistForUserProfile_ShouldReturnWatchlistSuccessfully() throws UserProfileNotFoundException {
        List<Watchlist> watchlists = List.of(watchlist);
        List<WatchlistDto> watchlistDtos = List.of(watchlistDto);

        when(watchlistRepository.findByUserProfileUsername("testuser")).thenReturn(watchlists);
        when(watchlistMapper.toListWatchlistDto(watchlists)).thenReturn(watchlistDtos);

        List<WatchlistDto> result = watchlistService.getWatchlistForUserProfile("testuser");

        assertEquals(1, result.size());
        assertEquals(watchlistDto, result.getFirst());
        verify(watchlistRepository).findByUserProfileUsername("testuser");
        verify(watchlistMapper).toListWatchlistDto(watchlists);
    }

    @Test
    void getWatchlistForUserProfile_ShouldReturnEmptyListWhenNoWatchlists() throws UserProfileNotFoundException {
        when(watchlistRepository.findByUserProfileUsername("testuser")).thenReturn(new ArrayList<>());

        List<WatchlistDto> result = watchlistService.getWatchlistForUserProfile("testuser");

        assertTrue(result.isEmpty());
        verify(watchlistRepository).findByUserProfileUsername("testuser");
        verifyNoInteractions(watchlistMapper);
    }

    @Test
    void addToWatchList_ShouldAddSuccessfully() throws AppException {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));
        when(watchlistMapper.storeToWatchlist(storeWatchlistDto)).thenReturn(watchlist);

        watchlistService.addToWatchList("testuser", storeWatchlistDto);

        verify(userProfileRepository).findByUsername("testuser");
        verify(watchlistMapper).storeToWatchlist(storeWatchlistDto);
        verify(watchlistRepository).save(watchlist);
        assertEquals(userProfile, watchlist.getUserProfile());
    }

    @Test
    void addToWatchList_ShouldThrowExceptionWhenUserNotFound() {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> 
            watchlistService.addToWatchList("testuser", storeWatchlistDto));

        verify(userProfileRepository).findByUsername("testuser");
        verifyNoInteractions(watchlistMapper);
        verifyNoInteractions(watchlistRepository);
    }

    @Test
    void addToWatchList_ShouldThrowExceptionOnError() {
        when(userProfileRepository.findByUsername("testuser")).thenThrow(new RuntimeException("Database error"));

        assertThrows(AppException.class, () -> 
            watchlistService.addToWatchList("testuser", storeWatchlistDto));

        verify(userProfileRepository).findByUsername("testuser");
    }

    @Test
    void removeFromWatchList_ShouldRemoveSuccessfully() throws AppException {
        userProfile.getWatchlists().add(watchlist);
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));

        watchlistService.removeFromWatchList("testuser", "123");

        assertTrue(userProfile.getWatchlists().isEmpty());
        verify(userProfileRepository).findByUsername("testuser");
        verify(watchlistRepository).delete(watchlist);
        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void removeFromWatchList_ShouldNotRemoveWhenShowNotInWatchlist() throws AppException {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));

        watchlistService.removeFromWatchList("testuser", "456");

        assertTrue(userProfile.getWatchlists().isEmpty());
        verify(userProfileRepository).findByUsername("testuser");
        verify(watchlistRepository, never()).delete(any());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void removeFromWatchList_ShouldThrowExceptionWhenUserNotFound() {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> 
            watchlistService.removeFromWatchList("testuser", "123"));

        verify(userProfileRepository).findByUsername("testuser");
        verifyNoInteractions(watchlistRepository);
    }

    @Test
    void removeFromWatchList_ShouldThrowExceptionOnInvalidShowId() {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));

        assertThrows(AppException.class, () -> 
            watchlistService.removeFromWatchList("testuser", "invalid"));

        verify(userProfileRepository).findByUsername("testuser");
    }
}