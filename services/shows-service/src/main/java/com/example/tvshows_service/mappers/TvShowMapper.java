package com.example.tvshows_service.mappers;

import com.example.tvshows_service.dto.TvShowDto;
import com.example.tvshows_service.dto.external.StoreWatchlistDto;
import com.example.tvshows_service.dto.external.TvMazeShowDto;
import com.example.tvshows_service.models.Genre;
import com.example.tvshows_service.models.TvShow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TvShowMapper {

    @Mapping(target = "id", source = "tvShowId")
    @Mapping(target = "genres", source = "genres")
    @Mapping(target = "watchlistUrl", ignore = true)
    TvShowDto tvShowToDto(TvShow tvShow);

    List<TvShowDto> tvShowsToDto(List<TvShow> tvShows);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tvShowId", source = "id")
    @Mapping(target = "genres", source = "genres")
    TvShow dtoToTvShow(TvShowDto tvShowDto);

    List<TvShow> tvShowsDtoToTvShows(List<TvShowDto> tvShowDtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tvShowId", source = "id")
    @Mapping(target = "rating", source = "rating.average")
    @Mapping(target = "imageMedium", source = "image.medium")
    @Mapping(target = "imageOriginal", source = "image.original")
    @Mapping(target = "imdb", source = "externals.imdb")
    @Mapping(target = "tvrage", source = "externals.tvrage")
    @Mapping(target = "thetvdb", source = "externals.thetvdb")
    @Mapping(target = "scheduleTime", source = "schedule.time")
    @Mapping(target = "scheduleDays", source = "schedule.days")
    @Mapping(target = "genres", ignore = true)
    TvShow mazeDtoToTvShow(TvMazeShowDto tvMazeShowDto);

    List<TvShow> mazeDtoToTvShowList(List<TvMazeShowDto> tvMazeShowDtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tvShowId", source = "id")
    @Mapping(target = "rating", source = "rating.average")
    @Mapping(target = "imageMedium", source = "image.medium")
    @Mapping(target = "imageOriginal", source = "image.original")
    @Mapping(target = "imdb", source = "externals.imdb")
    @Mapping(target = "tvrage", source = "externals.tvrage")
    @Mapping(target = "thetvdb", source = "externals.thetvdb")
    @Mapping(target = "scheduleTime", source = "schedule.time")
    @Mapping(target = "scheduleDays", source = "schedule.days")
    @Mapping(target = "genres", ignore = true)
    void updateTvShowFromMazeTvShowDto(TvMazeShowDto tvMazeShowDto, @MappingTarget TvShow tvShow);

    @Mapping(target = "description", source = "summary")
    StoreWatchlistDto tvShowToStoreWatchlistDto(TvShow tvShow);

    default List<String> mapGenresToNames(Set<Genre> genres) {
        if (genres == null) return null;
        return genres.stream()
                .map(Genre::getName)
                .collect(Collectors.toList());
    }

    default Set<Genre> mapNamesToGenres(List<String> names) {
        if (names == null) return null;
        return names.stream()
                .map(name -> {
                    Genre g = new Genre();
                    g.setName(name);
                    return g;
                })
                .collect(Collectors.toSet());
    }
}
