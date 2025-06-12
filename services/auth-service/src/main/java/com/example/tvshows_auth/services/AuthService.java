package com.example.tvshows_auth.services;

import com.example.tvshows_auth.config.UserAuthProvider;
import com.example.tvshows_auth.dto.*;
import com.example.tvshows_auth.models.User;
import com.example.tvshows_auth.exceptions.AppException;
import com.example.tvshows_auth.exceptions.UnsupportedVersionException;
import com.example.tvshows_auth.mappers.UserMapper;
import com.example.tvshows_auth.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        String token = this.userAuthProvider.createToken(userMapper.toUserDto(user));

        if (passwordEncoder.matches(credentialDto.password(), user.getPassword())) {
            LoginUserDto loginUserDto = userMapper.toLoginUserDto(user);
            loginUserDto.setToken(token);

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
}
