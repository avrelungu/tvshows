package com.example.user_service.repositories;

import com.example.user_service.models.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {
    public List<Watchlist> findByUserProfileUsername(String username);
}
