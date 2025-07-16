package com.example.user_service.dto;

import com.example.user_service.models.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreSearchHistoryUserEvent {
    private UserProfile userProfile;
}
