package com.shortener.service;

import com.shortener.model.db.User;
import com.shortener.model.request.LoginRequest;
import com.shortener.model.request.RegisterRequest;
import com.shortener.model.response.AuthResponse;
import com.shortener.repository.UserRepository;
import com.shortener.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should register new user successfully and return JWT token")
    void test_register_success_newUser() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("User@Example.com ")
                .password("password123")
                .build();

        String normalizedEmail = "user@example.com";
        String rawPassword = "password123";
        String hashedPassword = "hashedPassword";
        String generatedToken = "jwtToken";

        User userToSave = User.builder()
                .email(normalizedEmail)
                .password(hashedPassword)
                .role("ROLE_USER")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .email(normalizedEmail)
                .password(hashedPassword)
                .role("ROLE_USER")
                .build();

        doReturn(false).when(userRepository).existsByEmail(normalizedEmail);
        doReturn(hashedPassword).when(passwordEncoder).encode(rawPassword);
        doReturn(savedUser).when(userRepository).save(userToSave);
        doReturn(generatedToken).when(tokenProvider).generateToken(normalizedEmail, 1L);

        AuthResponse response = authService.register(registerRequest);

        AuthResponse expectedResponse = AuthResponse.builder()
                .token(generatedToken)
                .tokenType("Bearer")
                .email(normalizedEmail)
                .build();

        assertThat(response).isEqualTo(expectedResponse);
        verify(userRepository, times(1)).existsByEmail(normalizedEmail);
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(userToSave);
        verify(tokenProvider, times(1)).generateToken(normalizedEmail, 1L);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email is already registered")
    void test_register_fail_emailAlreadyExists() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .build();

        String email = "existing@example.com";

        doReturn(true).when(userRepository).existsByEmail(email);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already registered: " + email);

        verify(userRepository, times(1)).existsByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials and return JWT token")
    void test_login_success_validCredentials() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("User@Example.com ")
                .password("password123")
                .build();

        String normalizedEmail = "user@example.com";
        String rawPassword = "password123";
        String hashedPassword = "hashedPassword";
        String generatedToken = "jwtToken";

        User existingUser = User.builder()
                .id(1L)
                .email(normalizedEmail)
                .password(hashedPassword)
                .role("ROLE_USER")
                .build();

        doReturn(Optional.of(existingUser)).when(userRepository).findByEmail(normalizedEmail);
        doReturn(true).when(passwordEncoder).matches(rawPassword, hashedPassword);
        doReturn(generatedToken).when(tokenProvider).generateToken(normalizedEmail, 1L);

        AuthResponse response = authService.login(loginRequest);

        AuthResponse expectedResponse = AuthResponse.builder()
                .token(generatedToken)
                .tokenType("Bearer")
                .email(normalizedEmail)
                .build();

        assertThat(response).isEqualTo(expectedResponse);
        verify(userRepository, times(1)).findByEmail(normalizedEmail);
        verify(passwordEncoder, times(1)).matches(rawPassword, hashedPassword);
        verify(tokenProvider, times(1)).generateToken(normalizedEmail, 1L);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email is not found during login")
    void test_login_fail_userNotFound() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("notfound@example.com")
                .password("password123")
                .build();

        String email = "notfound@example.com";

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository, times(1)).findByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when password does not match during login")
    void test_login_fail_wrongPassword() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("user@example.com")
                .password("wrongPassword")
                .build();

        String email = "user@example.com";
        String wrongPassword = "wrongPassword";
        String hashedPassword = "hashedPassword";

        User existingUser = User.builder()
                .id(1L)
                .email(email)
                .password(hashedPassword)
                .role("ROLE_USER")
                .build();

        doReturn(Optional.of(existingUser)).when(userRepository).findByEmail(email);
        doReturn(false).when(passwordEncoder).matches(wrongPassword, hashedPassword);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(wrongPassword, hashedPassword);
        verifyNoInteractions(tokenProvider);
    }
}
