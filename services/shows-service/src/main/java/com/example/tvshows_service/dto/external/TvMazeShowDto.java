package com.example.tvshows_service.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvMazeShowDto {
    private Long id;
    private String url;
    private String name;
    private String type;
    private String language;
    private List<String> genres;
    private String status;
    private Integer runtime;
    private Integer averageRuntime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate premiered;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate ended;
    private String officialSite;
    private Schedule schedule;
    private Rating rating;
    private Integer weight;
    private Network network;
    private Object webChannel;
    private Object dvdCountry;
    private Externals externals;
    private Image image;
    private String summary;
    private Long updated;
    @JsonProperty("_links")
    private Links links;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Schedule {
        private String time;
        private List<String> days;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rating {
        private Double average;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Network {
        private Long id;
        private String name;
        private Country country;
        private String officialSite;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Country {
        private String name;
        private String code;
        private String timezone;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Externals {
        private Integer tvrage;
        private Integer thetvdb;
        private String imdb;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private String medium;
        private String original;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private Link self;
        private Link previousepisode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Link {
        private String href;
        private String name;
    }
}