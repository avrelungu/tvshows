package com.example.tvshows_auth.services;

import com.example.tvshows_auth.dto.SignUpDto;
import com.example.tvshows_auth.dto.UserDto;
import com.example.tvshows_auth.dto.UserProfileDto;
import com.example.tvshows_auth.enums.Membership;
import com.example.tvshows_auth.enums.Role;
import com.example.tvshows_auth.exceptions.AppException;
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

    public UserProfileDto getUserProfile(String username, String token) {
        return Objects.requireNonNull(webClient.get()
                        .uri(apiGatewayUrl + "/api/users/" + username + "/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .toEntity(UserProfileDto.class)
                        .block())
                .getBody();
    }

    public List<UserDto> getAllUsers(String requestingUsername, String requestingUserRole) {
        if (!requestingUserRole.equals("ADMIN")) {
            throw new AppException("Only admins can view all users", HttpStatus.FORBIDDEN);
        }

        return this.userRepository.findAll().stream()
                .filter(user -> !user.getUsername().equals(requestingUsername))
                .map(userMapper::toUserDto)
                .toList();
    }

    public UserDto promoteToAdmin(String username, String requestingUserRole) {
        // Check if requesting user is admin
        if (!requestingUserRole.equals("ADMIN")) {
            throw new AppException("Only admins can promote users", HttpStatus.FORBIDDEN);
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
