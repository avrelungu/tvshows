package com.example.tvshows_auth.controllers;

import com.example.tvshows_auth.config.UserAuthProvider;
import com.example.tvshows_auth.dto.CredentialDto;
import com.example.tvshows_auth.dto.LoginUserDto;
import com.example.tvshows_auth.dto.RefreshTokenDto;
import com.example.tvshows_auth.dto.SignUpDto;
import com.example.tvshows_auth.dto.UserDto;
import com.example.tvshows_auth.exceptions.UnsupportedVersionException;
import com.example.tvshows_auth.services.AuthService;
import com.example.tvshows_auth.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserDto> login(@RequestBody CredentialDto credentialDto, @RequestHeader(value = "X-API-Version", defaultValue = "v1") String apiVersion) throws UnsupportedVersionException {
        LoginUserDto userDto = authService.login(credentialDto, apiVersion);

        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody SignUpDto signUpDto, @RequestHeader(value = "X-API-Version", defaultValue = "v1") String apiVersion) throws UnsupportedVersionException {
        UserDto userDto = authService.register(signUpDto, apiVersion);

        return ResponseEntity.created(URI.create("/users/" + userDto.getId())).body(userDto);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDto> getUser(@PathVariable String username, @RequestHeader(value = "X-API-Version", defaultValue = "v1") String apiVersion) {
        UserDto userDto = authService.getUser(username);

        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginUserDto> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        LoginUserDto loginUserDto = authService.refreshToken(refreshTokenDto.getRefreshToken());

        return ResponseEntity.ok(loginUserDto);
    }

    @PostMapping("/promote/{username}")
    public ResponseEntity<UserDto> promoteToAdmin(@PathVariable String username, @RequestHeader(name = "X-Auth-Role") String role) {
        UserDto userDto = userService.promoteToAdmin(username, role);
        return ResponseEntity.ok(userDto);
    }
}
