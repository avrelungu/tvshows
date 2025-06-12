package com.example.user_service.services;

import com.example.user_service.dto.StoreUserProfileDto;
import com.example.user_service.dto.UserProfileDto;
import com.example.user_service.exceptions.AppException;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.mappers.UserProfileMapper;
import com.example.user_service.models.UserProfile;
import com.example.user_service.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final WebClient webClient;
    private final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    public UserProfileService(UserProfileRepository userProfileRepository, UserProfileMapper userProfileMapper, WebClient webClient) {
        this.userProfileRepository = userProfileRepository;
        this.userProfileMapper = userProfileMapper;
        this.webClient = webClient;
    }

    public UserProfile storeUserProfile(StoreUserProfileDto storeUserProfileDto) throws AppException {
        Optional<UserProfile> existingUserProfile = userProfileRepository.findByUsername(storeUserProfileDto.getUsername());

        if (existingUserProfile.isPresent()) {
            throw new AppException("User profile already exists", HttpStatus.CONFLICT);
        }

        UserProfile userProfile = userProfileMapper.userProfileDtoToUserProfile(storeUserProfileDto);

        return userProfileRepository.save(userProfile);
    }

    public UserProfileDto getProfile(String username) throws UserProfileNotFoundException {
        Optional<UserProfile> userProfile = userProfileRepository.findByUsername(username);

        if (userProfile.isEmpty()) {
            throw new UserProfileNotFoundException(username);
        }

        return userProfileMapper.profileToUserProfileDto(userProfile.get());
    }
}
