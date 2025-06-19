package com.example.user_service.mappers;

import com.example.user_service.dto.StoreUserProfileDto;
import com.example.user_service.dto.UserProfileDto;
import com.example.user_service.models.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    StoreUserProfileDto userProfileToStoreUserProfileDto(UserProfile userProfile);

    @Mapping(target = "id", ignore = true)
    UserProfile userProfileDtoToUserProfile(StoreUserProfileDto storeUserProfileDto);

    UserProfileDto profileToUserProfileDto(UserProfile userProfile);
}
