package com.example.user_service.mappers;

import com.example.user_service.dto.TvShowSearchHistoryDto;
import com.example.user_service.models.TvShowSearchHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TvShowSearchHistoryMapper {
    @Mapping(target = "username", source = "userProfile.username")
    TvShowSearchHistoryDto toDto(TvShowSearchHistory tvShowSearchHistory);
}
