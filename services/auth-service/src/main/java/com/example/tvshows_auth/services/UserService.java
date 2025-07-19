package com.example.tvshows_auth.services;

import com.example.tvshows_auth.dto.SignUpDto;
import com.example.tvshows_auth.dto.UserDto;
import com.example.tvshows_auth.dto.UserProfileDto;
import com.example.tvshows_auth.enums.Membership;
import com.example.tvshows_auth.enums.Role;
import com.example.tvshows_auth.exceptions.AppException;
import com.example.tvshows_auth.exceptions.InsufficientPermissionsException;
import com.example.tvshows_auth.mappers.UserMapper;
import com.example.tvshows_auth.models.User;
import com.example.tvshows_auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserMapper userMapper;

    @Value("${api-gateway.url}")
    private String apiGatewayUrl;

    private final WebClient webClient;

    private final UserRepository userRepository;

    public UserService(WebClient webClient, UserMapper userMapper, UserRepository userRepository) {
        this.webClient = webClient;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    public void createUserProfile(SignUpDto signUpDto, UserDto userDto) {

        UserProfileDto userProfileDto = userMapper.signUpToUserProfile(signUpDto);
        userProfileDto.setId(userDto.getId());

        webClient.post()
                .uri(apiGatewayUrl + "/api/users/profile")
                .bodyValue(userProfileDto)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + userDto.getToken())
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public UserProfileDto getUserProfile(String username, String token) {
        return Objects.requireNonNull(webClient.get()
                        .uri(apiGatewayUrl + "/api/users/" + username + "/profile")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                        .retrieve()
                        .toEntity(UserProfileDto.class)
                        .block())
                .getBody();
    }

    public List<UserDto> getAllUsers(String requestingUsername, String requestingUserRole) {
        if (!requestingUserRole.equals(Role.ADMIN.getValue())) {
            throw new InsufficientPermissionsException(Role.ADMIN.getValue(), "view all users");
        }

        return this.userRepository.findAll().stream()
                .filter(user -> !user.getUsername().equals(requestingUsername))
                .map(userMapper::toUserDto)
                .toList();
    }

    public UserDto promoteToAdmin(String username, String requestingUserRole) {
        // Check if requesting user is admin
        if (!requestingUserRole.equals(Role.ADMIN.getValue())) {
            throw new InsufficientPermissionsException(Role.ADMIN.getValue(), "promote users");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        user.setRole(Role.ADMIN);
        User savedUser = userRepository.save(user);

        return userMapper.toUserDto(savedUser);
    }

    public void upgradeMembership(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        user.setMembership(Membership.PREMIUM);
        User savedUser = userRepository.save(user);

        userMapper.toUserDto(savedUser);
    }
}
