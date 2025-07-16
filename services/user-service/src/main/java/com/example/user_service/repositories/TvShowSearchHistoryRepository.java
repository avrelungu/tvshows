package com.example.user_service.repositories;

import com.example.user_service.dto.StoreSearchHistoryUserEvent;
import com.example.user_service.models.TvShowSearchHistory;
import com.example.user_service.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TvShowSearchHistoryRepository extends JpaRepository<TvShowSearchHistory, UUID> {
    List<TvShowSearchHistory> findByUserProfileOrderBySearchTimeDesc(UserProfile userProfile);

    @Modifying
    @Query("DELETE FROM TvShowSearchHistory t where t.userProfile = :userProfile AND t.id NOT IN" +
        "(SELECT t2.id FROM TvShowSearchHistory t2 WHERE t2.userProfile = :userProfile ORDER BY t2.searchTime DESC LIMIT 10)"
    )
    void cleanUpSearchHistory(@Param("userProfile") UserProfile storeSearchHistoryUserEvent);
}
