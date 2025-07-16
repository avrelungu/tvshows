package com.example.user_service.services;

import com.example.user_service.dto.StoreSearchHistoryUserEvent;
import com.example.user_service.repositories.TvShowSearchHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TvShowSearchHistoryCleanUpService {


    private final TvShowSearchHistoryRepository tvShowSearchHistoryRepository;

    public TvShowSearchHistoryCleanUpService(TvShowSearchHistoryRepository tvShowSearchHistoryRepository) {
        this.tvShowSearchHistoryRepository = tvShowSearchHistoryRepository;
    }

    @Async
    @EventListener
    @Transactional
    public void cleanUpSearchHistory(StoreSearchHistoryUserEvent storeSearchHistoryUserEvent) {
        try {
            tvShowSearchHistoryRepository.cleanUpSearchHistory(storeSearchHistoryUserEvent.getUserProfile());
        } catch (Exception e) {
            log.error("Failed to clean up search history for user {}", e.getMessage());
        }
    }
}
