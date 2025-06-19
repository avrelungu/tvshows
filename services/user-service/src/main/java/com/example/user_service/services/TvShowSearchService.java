package com.example.user_service.services;

import com.example.user_service.dto.StoreShowsSearchDto;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.models.TvShowSearchHistory;
import com.example.user_service.models.UserProfile;
import com.example.user_service.repositories.TvShowSearchHistoryRepository;
import com.example.user_service.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TvShowSearchService {

    private final UserProfileRepository userProfileRepository;
    private final TvShowSearchHistoryRepository tvShowSearchHistoryRepository;

    public TvShowSearchService(
            UserProfileRepository userProfileRepository,
            TvShowSearchHistoryRepository tvShowSearchHistoryRepository
    ) {
        this.userProfileRepository = userProfileRepository;
        this.tvShowSearchHistoryRepository = tvShowSearchHistoryRepository;
    }


    public void storeTvShowSearch(StoreShowsSearchDto storeShowsSearchDto) throws UserProfileNotFoundException {
        UserProfile userProfile = userProfileRepository.findById(storeShowsSearchDto.getUserId())
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
}
