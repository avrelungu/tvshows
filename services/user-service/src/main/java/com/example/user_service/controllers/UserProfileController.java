package com.example.user_service.controllers;

import com.example.user_service.dto.StoreUserProfileDto;
import com.example.user_service.dto.UpgradeUserProfileDto;
import com.example.user_service.dto.UserProfileDto;
import com.example.user_service.exceptions.AppException;
import com.example.user_service.mappers.UserProfileMapper;
import com.example.user_service.models.UserProfile;
import com.example.user_service.services.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;


    public UserProfileController(UserProfileService userProfileService, UserProfileMapper userProfileMapper) {
        this.userProfileService = userProfileService;
        this.userProfileMapper = userProfileMapper;
    }

    @PostMapping("/profile")
    public ResponseEntity<StoreUserProfileDto> getProfile(@RequestBody StoreUserProfileDto storeUserProfileDto, @RequestHeader(value = "X-API-Version", defaultValue = "v1") String apiVersion) throws AppException {
        UserProfile userProfile = userProfileService.storeUserProfile(storeUserProfileDto);

        StoreUserProfileDto resultedUserProfile = userProfileMapper.userProfileToStoreUserProfileDto(userProfile);

        return ResponseEntity.ok(resultedUserProfile);
    }

    @GetMapping("/{username}/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable String username) throws AppException {
        UserProfileDto userProfileDto = userProfileService.getProfile(username);

        return ResponseEntity.ok(userProfileDto);
    }

    @PostMapping("/upgrade")
    public ResponseEntity<UserProfileDto> upgradeProfile(@RequestBody UpgradeUserProfileDto upgradeUserProfileDto) throws AppException {
        UserProfileDto userProfileDto = userProfileService.upgradeProfile(upgradeUserProfileDto);

        return ResponseEntity.ok(userProfileDto);
    }
}
