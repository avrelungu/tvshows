package com.example.tvshows_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
public class ShowsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShowsServiceApplication.class, args);
	}

}
