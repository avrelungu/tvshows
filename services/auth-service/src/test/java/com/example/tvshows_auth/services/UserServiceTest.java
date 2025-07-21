package com.example.tvshows_auth.services;

import com.example.tvshows_auth.dto.SignUpDto;
import com.example.tvshows_auth.dto.UserDto;
import com.example.tvshows_auth.dto.UserProfileDto;
import com.example.tvshows_auth.enums.Membership;
import com.example.tvshows_auth.enums.Role;
import com.example.tvshows_auth.exceptions.AppException;
import com.example.tvshows_auth.exceptions.InsufficientPermissionsException;
import com.example.tvshows_auth.mappers.UserMapper;
import com.example.tvshows_auth.models.User;
import com.example.tvshows_auth.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private WebClient webClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "apiGatewayUrl", "http://api-gateway");
    }

    @Test
    void createUserProfile_Success() {
        // Arrange
        SignUpDto signUpDto = new SignUpDto("John", "Doe", "johndoe", "john@example.com", "secret", "PREMIUM", "ADMIN");

        UserDto userDto = new UserDto();
        userDto.setId(UUID.randomUUID());
        userDto.setToken("test-token");

        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setId(userDto.getId());

        when(userMapper.signUpToUserProfile(signUpDto)).thenReturn(userProfileDto);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("http://api-gateway/api/users/profile")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));

        // Act
        assertDoesNotThrow(() -> userService.createUserProfile(signUpDto, userDto));

        // Assert
        verify(webClient).post();
        verify(requestHeadersSpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + userDto.getToken());
        assertEquals(userDto.getId(), userProfileDto.getId());
    }

    @Test
    void getUserProfile_Success() {
        // Arrange
        String username = "johndoe";
        String token = "test-token";
        UserProfileDto expectedProfile = new UserProfileDto();
        expectedProfile.setUsername(username);
        expectedProfile.setMemberType("FREE");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("http://api-gateway/api/users/" + username + "/profile")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(UserProfileDto.class))
                .thenReturn(Mono.just(ResponseEntity.ok(expectedProfile)));

        // Act
        UserProfileDto result = userService.getUserProfile(username, token);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("FREE", result.getMemberType());
        verify(requestHeadersSpec).header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    @Test
    void getAllUsers_AsAdmin_ReturnsUserList() {
        // Arrange
        String requestingUsername = "admin";
        String requestingUserRole = Role.ADMIN.getValue();

        User user1 = User.builder()
                .username("user1")
                .role(Role.USER)
                .build();
        User user2 = User.builder()
                .username("user2")
                .role(Role.USER)
                .build();
        User adminUser = User.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();

        List<User> allUsers = Arrays.asList(user1, user2, adminUser);

        UserDto userDto1 = new UserDto();
        userDto1.setUsername("user1");
        UserDto userDto2 = new UserDto();
        userDto2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(allUsers);
        when(userMapper.toUserDto(user1)).thenReturn(userDto1);
        when(userMapper.toUserDto(user2)).thenReturn(userDto2);

        // Act
        List<UserDto> result = userService.getAllUsers(requestingUsername, requestingUserRole);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(u -> u.getUsername().equals("admin")));
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_AsNonAdmin_ThrowsInsufficientPermissionsException() {
        // Arrange
        String requestingUsername = "user";
        String requestingUserRole = Role.USER.getValue();

        // Act & Assert
        assertThrows(InsufficientPermissionsException.class,
                () -> userService.getAllUsers(requestingUsername, requestingUserRole));

        verify(userRepository, never()).findAll();
    }

    @Test
    void promoteToAdmin_AsAdmin_Success() {
        // Arrange
        String targetUsername = "user";
        String requestingUserRole = Role.ADMIN.getValue();

        User user = User.builder()
                .username(targetUsername)
                .role(Role.USER)
                .build();

        UserDto expectedDto = new UserDto();
        expectedDto.setUsername(targetUsername);
        expectedDto.setRole(Role.ADMIN.getValue());

        when(userRepository.findByUsername(targetUsername)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDto(any(User.class))).thenReturn(expectedDto);

        // Act
        UserDto result = userService.promoteToAdmin(targetUsername, requestingUserRole);

        // Assert
        assertEquals(Role.ADMIN.getValue(), result.getRole());
        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void promoteToAdmin_AsNonAdmin_ThrowsInsufficientPermissionsException() {
        // Arrange
        String targetUsername = "user";
        String requestingUserRole = Role.USER.getValue();

        // Act & Assert
        assertThrows(InsufficientPermissionsException.class,
                () -> userService.promoteToAdmin(targetUsername, requestingUserRole));

        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void promoteToAdmin_UserNotFound_ThrowsAppException() {
        // Arrange
        String targetUsername = "nonexistent";
        String requestingUserRole = Role.ADMIN.getValue();

        when(userRepository.findByUsername(targetUsername)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> userService.promoteToAdmin(targetUsername, requestingUserRole));

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void upgradeMembership_Success() {
        // Arrange
        String username = "user";
        User user = User.builder()
                .username(username)
                .membership(Membership.FREE)
                .build();

        UserDto userDto = new UserDto();
        userDto.setMembership(Membership.PREMIUM.getValue());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        // Act
        assertDoesNotThrow(() -> userService.upgradeMembership(username));

        // Assert
        assertEquals(Membership.PREMIUM, user.getMembership());
        verify(userRepository).save(user);
        verify(userMapper).toUserDto(user);
    }

    @Test
    void upgradeMembership_UserNotFound_ThrowsAppException() {
        // Arrange
        String username = "nonexistent";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> userService.upgradeMembership(username));

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}