package com.example.tvshows_service.repositories;

import com.example.tvshows_service.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID>, JpaSpecificationExecutor<Genre> {
    List<Genre> findByNameIn(ArrayList<String> strings);
}
