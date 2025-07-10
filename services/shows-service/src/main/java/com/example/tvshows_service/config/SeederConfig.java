package com.example.tvshows_service.config;

import com.example.tvshows_service.dto.external.TvMazeShowDto;
import com.example.tvshows_service.repositories.TvShowRepository;
import com.example.tvshows_service.service.SeederService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
@Profile("dev")
@Slf4j
public class SeederConfig {

    @Value("${database.dump.path}")
    private String dumpFilename;

    TvShowRepository tvShowRepository;

    public SeederConfig(TvShowRepository tvShowRepository) {
        this.tvShowRepository = tvShowRepository;
    }

    @Bean
    CommandLineRunner generateJsonDump(ObjectMapper objectMapper, SeederService seederService) {
        return args -> {
            if (tvShowRepository.count() > 0) {
                return;
            }

            Path dumpPath = Paths.get(this.dumpFilename);

            if (Files.exists(dumpPath)) {
                try {
                    log.info("Loading TvShows database table from existing json dump.");

                    List<TvMazeShowDto> tvShows = objectMapper.readValue(
                            dumpPath.toFile(),
                            new TypeReference<List<TvMazeShowDto>>() {}
                    );
                    seederService.saveTvShows(tvShows);
                } catch (IOException e) {
                    log.error("Failed to save data from dump file: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    log.info("Generating TvShows database dump.");

                    List<TvMazeShowDto> tvShows = seederService.fetchAllTvShows();
                    Files.createDirectories(dumpPath.getParent());
                    objectMapper.writeValue(dumpPath.toFile(), tvShows);

                    seederService.saveTvShows(tvShows);

                    log.info("Wrote {} shows to TvShows database table.", tvShows.size());
                } catch (IOException e) {
                    log.error("Failed to generate data from dump file: {}", e.getMessage());
                }
            }
        };
    }
}
