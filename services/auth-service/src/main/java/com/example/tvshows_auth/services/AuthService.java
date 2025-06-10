package com.example.tvshows_auth.services;

import com.example.tvshows_auth.dto.CredentialDto;
import com.example.tvshows_auth.dto.SignUpDto;
import com.example.tvshows_auth.dto.UserDto;
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

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDto login(CredentialDto credentialDto, String apiVersion) throws UnsupportedVersionException {
        return switch (apiVersion) {
            case "v1" -> loginV1(credentialDto);
            default -> throw new UnsupportedVersionException(apiVersion);
        };
    }

    private UserDto loginV1(CredentialDto credentialDto) {
        User user = userRepository.findByUsername(credentialDto.username()).orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(credentialDto.password(), user.getPassword())) {
            return userMapper.toUserDto(user);
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

        return userMapper.toUserDto(savedUser);
    }
}
