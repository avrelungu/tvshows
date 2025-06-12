package com.example.user_service.mappers;

import com.example.user_service.dto.WatchlistDto;
import com.example.user_service.models.Watchlist;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WatchlistMapper {
    List<WatchlistDto> toListWatchlistDto(List<Watchlist> watchlist);
}
