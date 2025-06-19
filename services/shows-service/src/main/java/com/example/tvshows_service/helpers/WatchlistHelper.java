package com.example.tvshows_service.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WatchlistHelper {
    @Value("${api-gateway.url}")
    private String usersServiceUrl;

    public String createAddToWatchlistUrl(Long tvShowId, String username) {
        return usersServiceUrl + "/api/tv-shows/" + tvShowId + "/watchlist/" + username;
    }
}
