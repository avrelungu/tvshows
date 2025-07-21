package com.example.tvshows_auth.services;

import com.example.tvshows_auth.config.UserAuthProvider;
import com.example.tvshows_auth.dto.*;
import com.example.tvshows_auth.enums.Membership;
import com.example.tvshows_auth.enums.Role;
import com.example.tvshows_auth.exceptions.AppException;
import com.example.tvshows_auth.exceptions.UnsupportedVersionException;
import com.example.tvshows_auth.mappers.UserMapper;
import com.example.tvshows_auth.models.User;
import com.example.tvshows_auth.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserAuthProvider userAuthProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserDto testUserDto;
    private LoginUserDto testLoginUserDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .membership(Membership.FREE)
                .build();

        testUserDto = new UserDto();
        testUserDto.setId(testUser.getId());
        testUserDto.setUsername(testUser.getUsername());
        testUserDto.setFirstName(testUser.getFirstName());
        testUserDto.setLastName(testUser.getLastName());
        testUserDto.setEmail(testUser.getEmail());
        testUserDto.setRole(Role.USER.getValue());
        testUserDto.setMembership(Membership.FREE.getValue());

        testLoginUserDto = new LoginUserDto();
        testLoginUserDto.setUsername(testUser.getUsername());
        testLoginUserDto.setToken("test-token");
        testLoginUserDto.setRefreshToken("test-refresh-token");
        testLoginUserDto.setRole(Role.USER);
        testLoginUserDto.setMembership(Membership.FREE);
    }

    @Test
    void login_ValidCredentials_ReturnsLoginUserDto() throws UnsupportedVersionException {
        // Arrange
        CredentialDto credentialDto = new CredentialDto("testuser", "password");
        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setMemberType("FREE");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);
        when(userMapper.toLoginUserDto(testUser)).thenReturn(testLoginUserDto);
        when(userAuthProvider.createToken(testUserDto)).thenReturn("test-token");
        when(userAuthProvider.createRefreshToken(testUserDto)).thenReturn("test-refresh-token");
        when(userService.getUserProfile("testuser", "test-token")).thenReturn(profileDto);

        // Act
        LoginUserDto result = authService.login(credentialDto, "v1");

        // Assert
        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        assertEquals("test-refresh-token", result.getRefreshToken());
        assertEquals(Role.USER, result.getRole());
        assertEquals(Membership.FREE, result.getMembership());
        verify(userAuthProvider).createToken(testUserDto);
        verify(userAuthProvider).createRefreshToken(testUserDto);
    }

    @Test
    void login_InvalidPassword_ThrowsAppException() {
        // Arrange
        CredentialDto credentialDto = new CredentialDto("testuser", "wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(credentialDto, "v1"));

        assertEquals("Invalid password", exception.getMessage());
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());
    }

    @Test
    void login_UserNotFound_ThrowsAppException() {
        // Arrange
        CredentialDto credentialDto = new CredentialDto("nonexistent", "password");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> authService.login(credentialDto, "v1"));

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void login_UnsupportedVersion_ThrowsUnsupportedVersionException() {
        // Arrange
        CredentialDto credentialDto = new CredentialDto("testuser", "password");

        // Act & Assert
        assertThrows(UnsupportedVersionException.class,
                () -> authService.login(credentialDto, "v2"));
    }

    @Test
    void register_NewUser_ReturnsUserDto() throws UnsupportedVersionException {
        // Arrange
        SignUpDto signUpDto = new SignUpDto("John", "Doe", "johndoe", "john@example.com", "secret", "PREMIUM", "ADMIN");

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(userMapper.signUpToUser(signUpDto)).thenReturn(testUser);
        when(passwordEncoder.encode("secret")).thenReturn("secret");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);
        when(userAuthProvider.createToken(testUserDto)).thenReturn("test-token");

        // Act
        UserDto result = authService.register(signUpDto, "v1");

        // Assert
        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        verify(userService).createUserProfile(signUpDto, testUserDto);
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ExistingUser_ThrowsAppException() {
        // Arrange
        SignUpDto signUpDto = new SignUpDto("John", "Doe", "existinguser", "john@example.com", "secret", "PREMIUM", "ADMIN");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> authService.register(signUpDto, "v1"));

        assertEquals("User already exists", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUser_ExistingUser_ReturnsUserDto() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);

        // Act
        UserDto result = authService.getUser("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUser_NonExistingUser_ThrowsAppException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> authService.getUser("nonexistent"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_ValidToken_ReturnsNewLoginUserDto() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setMemberType("PREMIUM");

        when(userAuthProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(userAuthProvider.getUsernameFromRefreshToken(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);
        when(userMapper.toLoginUserDto(testUser)).thenReturn(testLoginUserDto);
        when(userAuthProvider.createToken(testUserDto)).thenReturn("new-token");
        when(userAuthProvider.createRefreshToken(testUserDto)).thenReturn("new-refresh-token");
        when(userService.getUserProfile("testuser", "new-token")).thenReturn(profileDto);

        // Act
        LoginUserDto result = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals("new-token", result.getToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        assertEquals(Membership.PREMIUM, result.getMembership());
    }

    @Test
    void refreshToken_InvalidToken_ThrowsAppException() {
        // Arrange
        String refreshToken = "invalid-token";

        when(userAuthProvider.validateRefreshToken(refreshToken)).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> authService.refreshToken(refreshToken));

        assertEquals("Invalid refresh token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void refreshToken_NoUsernameInToken_ThrowsAppException() {
        // Arrange
        String refreshToken = "token-without-username";

        when(userAuthProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(userAuthProvider.getUsernameFromRefreshToken(refreshToken)).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> authService.refreshToken(refreshToken));

        assertEquals("Cannot extract user from refresh token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }
}
