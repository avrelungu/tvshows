package com.example.tvshows_service.specifications;

import com.example.tvshows_service.models.Genre;
import com.example.tvshows_service.models.TvShow;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class TvShowSpecification {

    public static Specification<TvShow> hasName(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<TvShow> hasDescription(String description) {
        return (root, query, cb) -> description == null ? null : cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<TvShow> premieredAfter(LocalDateTime premiered) {
        return (root, query, cb) -> premiered == null ? null : cb.greaterThanOrEqualTo(root.get("premiered"), premiered);
    }

    public static Specification<TvShow> endedBefore(LocalDateTime ended) {
        return (root, query, cb) -> ended == null ? null : cb.lessThanOrEqualTo(root.get("ended"), ended);
    }

    public static Specification<TvShow> ratingBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min != null && max != null) return cb.between(root.get("rating"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("rating"), min);
            if (max != null) return cb.lessThanOrEqualTo(root.get("rating"), max);
            return null;
        };
    }

    public static Specification<TvShow> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(cb.lower(root.get("status")), status.toLowerCase());
    }

    public static Specification<TvShow> hasLanguage(String language) {
        return (root, query, cb) -> language == null ? null : cb.equal(cb.lower(root.get("language")), language.toLowerCase());
    }

    public static Specification<TvShow> hasNetwork(String network) {
        return (root, query, cb) -> network == null ? null : cb.equal(cb.lower(root.get("network")), network.toLowerCase());
    }

    public static Specification<TvShow> hasGenres(List<String> genres) {
        return (root, query, cb) -> {
            if (genres == null || genres.isEmpty()) return null;

            Join<TvShow, Genre> genreJoin = root.join("genres", JoinType.INNER);

            List<String> lowerGenres = genres.stream()
                    .map(String::toLowerCase)
                    .toList();

            return genreJoin.get("name").in(lowerGenres);
        };
    }
}
