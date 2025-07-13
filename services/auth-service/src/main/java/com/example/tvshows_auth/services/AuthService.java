package com.example.tvshows_auth.services;

import com.example.tvshows_auth.config.UserAuthProvider;
import com.example.tvshows_auth.dto.*;
import com.example.tvshows_auth.models.User;
import com.example.tvshows_auth.exceptions.AppException;
import com.example.tvshows_auth.exceptions.UnsupportedVersionException;
import com.example.tvshows_auth.mappers.UserMapper;
import com.example.tvshows_auth.repositories.UserRepository;
import com.example.tvshows_auth.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, UserService userService, UserAuthProvider userAuthProvider) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userService = userService;
        this.userAuthProvider = userAuthProvider;
    }

    public LoginUserDto login(CredentialDto credentialDto, String apiVersion) throws UnsupportedVersionException {
        return switch (apiVersion) {
            case "v1" -> loginV1(credentialDto);
            default -> throw new UnsupportedVersionException(apiVersion);
        };
    }

    private LoginUserDto loginV1(CredentialDto credentialDto) {
        User user = userRepository.findByUsername(credentialDto.username()).orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(credentialDto.password(), user.getPassword())) {
            UserDto userDto = userMapper.toUserDto(user);
            String token = this.userAuthProvider.createToken(userDto);
            String refreshToken = this.userAuthProvider.createRefreshToken(userDto);

            LoginUserDto loginUserDto = userMapper.toLoginUserDto(user);
            loginUserDto.setToken(token);
            loginUserDto.setRefreshToken(refreshToken);
            loginUserDto.setRole(user.getRole());

            return loginUserDto;
        }

        throw new AppException("Invalid password", HttpStatus.BAD_GATEWAY);
    }

    public UserDto register(SignUpDto signUpDto, String apiVersion) throws UnsupportedVersionException {
        return switch (apiVersion) {
            case "v1" -> registerV1(signUpDto);
            default -> throw new UnsupportedVersionException(apiVersion);
        };
    }

    private UserDto registerV1(SignUpDto signUpDto) {
        Optional<User> oUser = userRepository.findByUsername(signUpDto.username());

        if (oUser.isPresent()) {
            throw new AppException("User already exists", HttpStatus.CONFLICT);
        }

        User user = userMapper.signUpToUser(signUpDto);

        log.info("Registered user: {}", user);

        user.setPassword(passwordEncoder.encode(signUpDto.password()));

        User savedUser = userRepository.save(user);

        UserDto userDto = userMapper.toUserDto(savedUser);

        userDto.setToken(userAuthProvider.createToken(userDto));

        UserProfileDto userProfileDto = userService.createUserProfile(signUpDto, userDto);

        return userDto;
    }

    public UserDto getUser(String username) {
        Optional<User> oUser = userRepository.findByUsername(username);

        if (oUser.isPresent()) {
            return userMapper.toUserDto(oUser.get());
        }

        throw new AppException("Unknown user", HttpStatus.NOT_FOUND);
    }

    public LoginUserDto refreshToken(String refreshToken) {
        if (!userAuthProvider.validateRefreshToken(refreshToken)) {
            throw new AppException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        String username = userAuthProvider.getUsernameFromRefreshToken(refreshToken);
        if (username == null) {
            throw new AppException("Cannot extract user from refresh token", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        UserDto userDto = userMapper.toUserDto(user);
        String newAccessToken = userAuthProvider.createToken(userDto);
        String newRefreshToken = userAuthProvider.createRefreshToken(userDto);

        LoginUserDto loginUserDto = userMapper.toLoginUserDto(user);
        loginUserDto.setToken(newAccessToken);
        loginUserDto.setRefreshToken(newRefreshToken);
        loginUserDto.setRole(user.getRole());

        return loginUserDto;
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

    public List<UserDto> getAllUsers(String requestingUserRole) {
        // Check if requesting user is admin
        if (!requestingUserRole.equals("ADMIN")) {
            throw new AppException("Only admins can view all users", HttpStatus.FORBIDDEN);
        }

        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }
}
