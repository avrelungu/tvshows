package com.example.tvshows_auth.dto;

public record SignUpDto(String firstName, String lastName, String username, String email, String password, String memberType) {
}
