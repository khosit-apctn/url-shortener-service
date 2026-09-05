package com.shortener.service;

import com.shortener.security.JwtTokenProvider;
import com.shortener.model.db.Users;
import com.shortener.model.request.LoginRequest;
import com.shortener.model.request.RegisterRequest;
import com.shortener.model.response.AuthResponse;
import com.shortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        String email = registerRequest.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered: " + email);
        }

        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        Users users = Users.builder()
                .email(email)
                .password(hashedPassword)
                .role("ROLE_USER")
                .build();

        Users savedUsers = userRepository.save(users);

        String token = tokenProvider.generateToken(savedUsers.getEmail(), savedUsers.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(savedUsers.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();

        Users users = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), users.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(users.getEmail(), users.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(users.getEmail())
                .build();
    }
}