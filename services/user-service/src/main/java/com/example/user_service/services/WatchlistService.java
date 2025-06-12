package com.example.user_service.services;

import com.example.user_service.dto.WatchlistDto;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.mappers.WatchlistMapper;
import com.example.user_service.models.UserProfile;
import com.example.user_service.models.Watchlist;
import com.example.user_service.repositories.UserProfileRepository;
import com.example.user_service.repositories.WatchlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WatchlistService {
    private final UserProfileRepository userProfileRepository;
    private final WatchlistRepository watchlistRepository;

    private final WatchlistMapper watchlistMapper;

    public WatchlistService(WatchlistRepository watchlistRepository, UserProfileRepository userProfileRepository, WatchlistMapper watchlistMapper) {
        this.watchlistRepository = watchlistRepository;
        this.userProfileRepository = userProfileRepository;
        this.watchlistMapper = watchlistMapper;
    }

    public List<WatchlistDto> getWatchlistForUserProfile(String username) throws UserProfileNotFoundException {
        List<Watchlist> watchlists = watchlistRepository.findByUserProfileUsername(username);

        return watchlistMapper.toListWatchlistDto(watchlists);
    }
}
