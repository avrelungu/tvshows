package com.example.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreUserProfileDto {
    private UUID id;
    private String username;
    private String memberType;
    private String firstName;
    private String lastName;
    private String email;
}
