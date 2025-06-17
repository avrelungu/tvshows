package com.example.tvshows_service.service;

import com.example.tvshows_service.exceptions.TvShowsNotFoundException;
import com.example.tvshows_service.filters.TvShowFilter;
import com.example.tvshows_service.models.TvShow;
import com.example.tvshows_service.repositories.TvShowRepository;
import com.example.tvshows_service.specifications.TvShowSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Stream;

@Service
public class TvShowService {
    private final TvShowRepository tvShowRepository;

    public TvShowService(
            TvShowRepository tvShowRepository
    ) {
        this.tvShowRepository = tvShowRepository;
    }

    public Page<TvShow> getTopRatedShows(int page, int size) throws TvShowsNotFoundException {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        Page<TvShow> tvShowsPage = tvShowRepository.findAll(pageable);

        if (tvShowsPage.isEmpty()) {
            throw new TvShowsNotFoundException();
        }

        return tvShowsPage;
    }

    public Page<TvShow> getTvShows(int page, int size, TvShowFilter filter) throws TvShowsNotFoundException {
        Pageable pageable = createPageable(page, size, filter.getSortBy(), filter.getSortOrder());

        Specification<TvShow> spec = Stream.of(
                        TvShowSpecification.hasName(filter.getName()),
                        TvShowSpecification.hasDescription(filter.getDescription()),
                        TvShowSpecification.hasNetwork(filter.getNetwork()),
                        TvShowSpecification.hasStatus(filter.getStatus()),
                        TvShowSpecification.endedBefore(filter.getEnded()),
                        TvShowSpecification.premieredAfter(filter.getPremiered()),
                        TvShowSpecification.hasLanguage(filter.getLanguage()),
                        TvShowSpecification.ratingBetween(filter.getMinRating(), filter.getMaxRating()),
                        TvShowSpecification.hasGenres(filter.getGenres())
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(null);

        Page<TvShow> tvShowPage = tvShowRepository.findAll(spec, pageable);

        if (tvShowPage.isEmpty()) {
            throw new TvShowsNotFoundException();
        }

        return tvShowPage;
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortOrder) {
        Sort sort = Sort.unsorted();

        if (sortBy != null && !sortBy.isBlank()) {
            sort = Sort.by(sortBy);
            if ("desc".equalsIgnoreCase(sortOrder)) {
                sort = sort.descending();
            } else {
                sort = sort.ascending();
            }
        }

        return PageRequest.of(page, size, sort);
    }
}
