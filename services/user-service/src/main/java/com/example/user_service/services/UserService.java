package com.example.user_service.services;

import com.example.user_service.dtos.UserProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserService {
    @Value("${auth_service/url}")
    private String authServiceUrl;

    private final WebClient webClient;

    public UserService(WebClient webClient) {
        this.webClient = webClient;
    }

    public UserProfileDto getUserProfile() {
        this.webClient.get()
                .uri(this.authServiceUrl + "/auth/user")
                .retrieve()
                .block();
    }
}
