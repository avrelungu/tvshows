package com.example.user_service.services;

import com.example.user_service.dto.StoreUserProfileDto;
import com.example.user_service.dto.UpgradeUserProfileDto;
import com.example.user_service.dto.UserProfileDto;
import com.example.user_service.enums.MemberType;
import com.example.user_service.exceptions.AlreadyPremiumMemberException;
import com.example.user_service.exceptions.AppException;
import com.example.user_service.exceptions.UserProfileNotFoundException;
import com.example.user_service.mappers.UserProfileMapper;
import com.example.user_service.models.UserProfile;
import com.example.user_service.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    private UserProfile userProfile;
    private StoreUserProfileDto storeUserProfileDto;
    private UpgradeUserProfileDto upgradeUserProfileDto;
    private UserProfileDto userProfileDto;

    @BeforeEach
    void setUp() {
        userProfile = new UserProfile();
        userProfile.setUsername("testuser");
        userProfile.setMemberType(MemberType.FREE);

        storeUserProfileDto = new StoreUserProfileDto();
        storeUserProfileDto.setUsername("testuser");

        upgradeUserProfileDto = new UpgradeUserProfileDto();
        upgradeUserProfileDto.setUsername("testuser");

        userProfileDto = new UserProfileDto();
        userProfileDto.setUsername("testuser");
    }

    @Test
    void storeUserProfile_ShouldStoreSuccessfully() throws AppException {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userProfileMapper.userProfileDtoToUserProfile(storeUserProfileDto)).thenReturn(userProfile);
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);

        UserProfile result = userProfileService.storeUserProfile(storeUserProfileDto);

        assertEquals(userProfile, result);
        verify(userProfileRepository).findByUsername("testuser");
        verify(userProfileMapper).userProfileDtoToUserProfile(storeUserProfileDto);
        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void storeUserProfile_ShouldThrowExceptionWhenUserExists() {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));

        AppException exception = assertThrows(AppException.class, () -> 
            userProfileService.storeUserProfile(storeUserProfileDto));

        assertEquals("User profile already exists", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(userProfileRepository).findByUsername("testuser");
        verifyNoInteractions(userProfileMapper);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void getProfile_ShouldReturnProfileSuccessfully() throws UserProfileNotFoundException {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));
        when(userProfileMapper.profileToUserProfileDto(userProfile)).thenReturn(userProfileDto);

        UserProfileDto result = userProfileService.getProfile("testuser");

        assertEquals(userProfileDto, result);
        verify(userProfileRepository).findByUsername("testuser");
        verify(userProfileMapper).profileToUserProfileDto(userProfile);
    }

    @Test
    void getProfile_ShouldThrowExceptionWhenUserNotFound() {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class, () -> 
            userProfileService.getProfile("testuser"));

        verify(userProfileRepository).findByUsername("testuser");
        verifyNoInteractions(userProfileMapper);
    }

    @Test
    void upgradeProfile_ShouldUpgradeSuccessfully() throws AppException {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);
        when(userProfileMapper.profileToUserProfileDto(userProfile)).thenReturn(userProfileDto);

        UserProfileDto result = userProfileService.upgradeProfile(upgradeUserProfileDto);

        assertEquals(userProfileDto, result);
        assertEquals(MemberType.PREMIUM, userProfile.getMemberType());
        verify(userProfileRepository).findByUsername("testuser");
        verify(userProfileRepository).save(userProfile);
        verify(userProfileMapper).profileToUserProfileDto(userProfile);
    }

    @Test
    void upgradeProfile_ShouldThrowExceptionWhenUserNotFound() {
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class, () -> 
            userProfileService.upgradeProfile(upgradeUserProfileDto));

        verify(userProfileRepository).findByUsername("testuser");
        verify(userProfileRepository, never()).save(any());
        verifyNoInteractions(userProfileMapper);
    }

    @Test
    void upgradeProfile_ShouldThrowExceptionWhenAlreadyPremium() {
        userProfile.setMemberType(MemberType.PREMIUM);
        when(userProfileRepository.findByUsername("testuser")).thenReturn(Optional.of(userProfile));

        assertThrows(AlreadyPremiumMemberException.class, () -> 
            userProfileService.upgradeProfile(upgradeUserProfileDto));

        verify(userProfileRepository).findByUsername("testuser");
        verify(userProfileRepository, never()).save(any());
        verifyNoInteractions(userProfileMapper);
    }
}