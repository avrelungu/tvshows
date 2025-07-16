package com.example.tvshows_auth.controllers;

import com.example.tvshows_auth.dto.UserDto;
import com.example.tvshows_auth.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<List<UserDto>> getAllUsers(@RequestHeader(name = "X-Auth-Role") String role, @RequestHeader(name = "X-Auth-Username") String username) {
        List<UserDto> users = userService.getAllUsers(username, role);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{username}/upgrade")
    public ResponseEntity<Object> upgradeMembership(@PathVariable String username) {
        userService.upgradeMembership(username);

        return ResponseEntity.noContent().build();
    }
}
