package com.example.user_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private String firstName;
    private String lastName;
    private String username;
    private String memberType;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
