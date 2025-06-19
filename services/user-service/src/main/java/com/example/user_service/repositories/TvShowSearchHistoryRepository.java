package com.example.user_service.repositories;

import com.example.user_service.models.TvShowSearchHistory;
import com.example.user_service.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TvShowSearchHistoryRepository extends JpaRepository<TvShowSearchHistory, UUID> {
    List<TvShowSearchHistory> findByUserProfileOrderBySearchTimeDesc(UserProfile userProfile);
}
