package com.example.tvshows_service.assembler;

import com.example.tvshows_service.dto.TvShowDto;
import com.example.tvshows_service.mappers.TvShowMapper;
import com.example.tvshows_service.models.TvShow;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TvShowModelAssembler implements RepresentationModelAssembler<TvShow, TvShowDto>{
    private final TvShowMapper tvShowMapper;

    public TvShowModelAssembler(TvShowMapper tvShowMapper) {
        this.tvShowMapper = tvShowMapper;
    }

    @Override
    public TvShowDto toModel(TvShow entity) {
        return tvShowMapper.tvShowToDto(entity);
    }
}
