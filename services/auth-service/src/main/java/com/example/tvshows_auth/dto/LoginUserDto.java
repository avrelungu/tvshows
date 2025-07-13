package com.example.tvshows_auth.dto;

import com.example.tvshows_auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDto {
    private String username;
    private String firstName;
    private String lastName;
    private String token;
    private String refreshToken;
    private String email;
    private Role role;
}
