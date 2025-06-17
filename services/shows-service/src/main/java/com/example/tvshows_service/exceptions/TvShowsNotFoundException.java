package com.example.tvshows_service.exceptions;

import org.springframework.http.HttpStatus;

public class TvShowsNotFoundException extends AppException {

    public TvShowsNotFoundException() {
        super("TvShows not found", HttpStatus.NOT_FOUND);
    }
}
