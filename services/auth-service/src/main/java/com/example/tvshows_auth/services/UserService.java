package com.example.tvshows_auth.services;

import com.example.tvshows_auth.dto.SignUpDto;
import com.example.tvshows_auth.dto.UserDto;
import com.example.tvshows_auth.dto.UserProfileDto;
import com.example.tvshows_auth.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.UUID;

@Service
public class UserService {
    private final UserMapper userMapper;

    @Value("${api-gateway.url}")
    private String apiGatewayUrl;

    private final WebClient webClient;

    public UserService(WebClient webClient, UserMapper userMapper) {
        this.webClient = webClient;
        this.userMapper = userMapper;
    }

    public UserProfileDto createUserProfile(SignUpDto signUpDto, UserDto userDto) {

        UserProfileDto userProfileDto = userMapper.signUpToUserProfile(signUpDto);
        userProfileDto.setId(userDto.getId());

        return Objects.requireNonNull(webClient.post()
                        .uri(apiGatewayUrl + "/api/users/profile")
                        .bodyValue(userProfileDto)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userDto.getToken())
                        .retrieve()
                        .toEntity(UserProfileDto.class)
                        .block())
                .getBody();
    }
}
