package com.example.tvshows_service.repositories;

import com.example.tvshows_service.models.TvShow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TvShowRepository extends JpaRepository<TvShow, UUID>, JpaSpecificationExecutor<TvShow> {
    Optional<TvShow> findByName(String name);

    Optional<TvShow> findByTvShowId(Long tvShowId);

    Optional<List<TvShow>> findAllByTvShowIdIn(List<Long> tvShowIds);
}
