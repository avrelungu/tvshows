package com.example.user_service.services;

import com.example.user_service.dto.StoreShowsSearchDto;
import com.example.user_service.dto.TvShowSearchHistoryDto;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.mappers.TvShowSearchHistoryMapper;
import com.example.user_service.models.TvShowSearchHistory;
import com.example.user_service.models.UserProfile;
import com.example.user_service.repositories.TvShowSearchHistoryRepository;
import com.example.user_service.repositories.UserProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TvShowSearchService {

    private final UserProfileRepository userProfileRepository;
    private final TvShowSearchHistoryRepository tvShowSearchHistoryRepository;
    private final TvShowSearchHistoryMapper tvShowSearchHistoryMapper;

    public TvShowSearchService(
            UserProfileRepository userProfileRepository,
            TvShowSearchHistoryRepository tvShowSearchHistoryRepository,
            TvShowSearchHistoryMapper tvShowSearchHistoryMapper) {
        this.userProfileRepository = userProfileRepository;
        this.tvShowSearchHistoryRepository = tvShowSearchHistoryRepository;
        this.tvShowSearchHistoryMapper = tvShowSearchHistoryMapper;
    }


    public void storeTvShowSearch(String username, StoreShowsSearchDto storeShowsSearchDto) throws UserProfileNotFoundException {
        log.info("Storing tv show search for {}", username);
        log.info(storeShowsSearchDto.toString());

        UserProfile userProfile = userProfileRepository.findByUsername(username)
                .orElseThrow(UserProfileNotFoundException::new);

        TvShowSearchHistory tvShowSearchHistory = new TvShowSearchHistory();
        tvShowSearchHistory.setUserProfile(userProfile);
        tvShowSearchHistory.setSearchTime(storeShowsSearchDto.getSearchTime());
        tvShowSearchHistory.setFilters(storeShowsSearchDto.getFilters());
        tvShowSearchHistory.setEndpoint(storeShowsSearchDto.getEndpoint());

        tvShowSearchHistoryRepository.save(tvShowSearchHistory);

        List<TvShowSearchHistory> tvShowSearchHistoryList = tvShowSearchHistoryRepository.findByUserProfileOrderBySearchTimeDesc(userProfile);
        if (tvShowSearchHistoryList.size() > 10) {
            tvShowSearchHistoryRepository.deleteAll(tvShowSearchHistoryList.subList(10, tvShowSearchHistoryList.size()));
        }
    }

    public List<TvShowSearchHistoryDto> getSearchHistory(String username) throws UserProfileNotFoundException {
        UserProfile userProfile = userProfileRepository.findByUsername(username).orElseThrow(UserProfileNotFoundException::new);

        return tvShowSearchHistoryRepository.findByUserProfileOrderBySearchTimeDesc(userProfile)
                .stream().map(tvShowSearchHistoryMapper::toDto).toList();
    }
}
