package com.example.tvshows_auth.controllers;

import com.example.tvshows_auth.config.UserAuthProvider;
import com.example.tvshows_auth.dto.CredentialDto;
import com.example.tvshows_auth.dto.SignUpDto;
import com.example.tvshows_auth.dto.UserDto;
import com.example.tvshows_auth.exceptions.UnsupportedVersionException;
import com.example.tvshows_auth.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final UserAuthProvider userAuthProvider;

    public AuthController(AuthService authService, UserAuthProvider userAuthProvider) {
        this.authService = authService;
        this.userAuthProvider = userAuthProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody CredentialDto credentialDto, @RequestHeader(value = "X-API-Version", defaultValue = "v1") String apiVersion) throws UnsupportedVersionException {
        UserDto userDto = authService.login(credentialDto, apiVersion);
        userDto.setToken(this.userAuthProvider.createToken(userDto));

        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody SignUpDto signUpDto, @RequestHeader(value = "X-API-Version", defaultValue = "v1") String apiVersion) throws UnsupportedVersionException {
        UserDto userDto = authService.register(signUpDto, apiVersion);
        userDto.setToken(this.userAuthProvider.createToken(userDto));

        return ResponseEntity.created(URI.create("/users/" + userDto.getId())).body(userDto);
    }
}
