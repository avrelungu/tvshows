package com.example.tvshows_service.service;

import com.example.tvshows_service.dto.TvShowDto;
import com.example.tvshows_service.filters.TvShowFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TvShowCacheService {
    @Value("${redis.cache-keys.top-rated-shows.cache-key}")
    private String TOP_RATED_CACHE_KEY;

    @Value("${redis.cache-keys.top-rated-shows.ttl}")
    private long CACHE_TTL;

    @Value("${redis.cache-keys.filtered-shows.cache-key}")
    private String FILTERED_SHOWS_CACHE_KEY;

    @Value("${redis.cache-keys.filtered-shows.ttl}")
    private long FILTERED_SHOWS_CACHE_TTL;

    private final RedisTemplate<String, Object> redisTemplate;

    public TvShowCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public Page<TvShowDto> getCachedTopRatedShows(int page, int size) {

        return null;
//        String cacheKey = buildCacheKey(page, size);
//
//        try {
//            Object cached = redisTemplate.opsForValue().get(cacheKey);
//
//            if (cached != null) {
//                return null;
//            }
//
//            return (Page<TvShowDto>) cached;
//        } catch (Exception e) {
//            log.error("Error retrieving Top Rated TvShows from cache: {}", e.getMessage());
//
//            return null;
//        }
    }

    @SuppressWarnings("unchecked")
    public Page<TvShowDto> getCachedFilteredShows(int page, int size, TvShowFilter filter) {
        String filteredShowsCacheKey = buildFilteredShowsCacheKey(page, size, filter);

        try {
            return (Page<TvShowDto>) redisTemplate.opsForValue().get(filteredShowsCacheKey);
        } catch (Exception e) {
            log.error("Error retrieving FilteredShows from cache: {}", e.getMessage());

            return null;
        }
    }

    public void cacheTopRatedShows(int page, int size, Page<TvShowDto> tvShowsPage) {
        String cacheKey = buildCacheKey(page, size);

        try {
            this.redisTemplate.opsForValue().set(cacheKey, tvShowsPage, CACHE_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Error caching Top Rated TvShows: {}", e.getMessage());
        }
    }

    public void cacheFilteredTvShows(int page, int size, TvShowFilter filter, Page<TvShowDto> tvShowPage) {
        String cacheKey = buildFilteredShowsCacheKey(page, size, filter);

        try {
            redisTemplate.opsForValue().set(cacheKey, tvShowPage, FILTERED_SHOWS_CACHE_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Error caching FilteredShows from cache: {}", e.getMessage());
        }
    }

    private String buildFilteredShowsCacheKey(int page, int size, TvShowFilter filter) {
        int filterHash = filter != null ? filter.hashCode() : 0;
        return String.format("%s:page_%d:size_%d:filter_%d", FILTERED_SHOWS_CACHE_KEY, page, size, filterHash);
    }

    private String buildCacheKey(int page, int size) {
        return String.format("%s:page_%d:size_%d", TOP_RATED_CACHE_KEY, page, size);
    }
}
