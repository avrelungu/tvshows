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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WatchlistService {
    private final WatchlistRepository watchlistRepository;

    private final WatchlistMapper watchlistMapper;

    private final UserProfileRepository userProfileRepository;

    public WatchlistService(
            WatchlistRepository watchlistRepository,
            UserProfileRepository userProfileRepository,
            WatchlistMapper watchlistMapper
    ) {
        this.watchlistRepository = watchlistRepository;
        this.watchlistMapper = watchlistMapper;
        this.userProfileRepository = userProfileRepository;
    }

    public List<WatchlistDto> getWatchlistForUserProfile(String username) throws UserProfileNotFoundException {
        List<Watchlist> watchlists = watchlistRepository.findByUserProfileUsername(username);

        return watchlistMapper.toListWatchlistDto(watchlists);
    }

    public void addToWatchList(String username, StoreWatchlistDto storeWatchlistDto) throws AppException {
        try {
            UserProfile userProfile = userProfileRepository.findByUsername(username)
                    .orElseThrow(UserProfileNotFoundException::new);

            Watchlist watchlist = watchlistMapper.storeToWatchlist(storeWatchlistDto);

            watchlist.setUserProfile(userProfile);

            watchlistRepository.save(watchlist);
        } catch (Exception e) {
            throw new AppException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
