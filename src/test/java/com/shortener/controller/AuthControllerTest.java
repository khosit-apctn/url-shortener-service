package com.shortener.controller;
 
import com.shortener.common.exception.GlobalExceptionHandler;
import com.shortener.model.request.LoginRequest;
import com.shortener.model.request.RegisterRequest;
import com.shortener.model.response.AuthResponse;
import com.shortener.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("POST /api/register should return 201 Created and JWT token")
    void test_register_success_return201AndToken() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwtToken")
                .tokenType("Bearer")
                .email("user@example.com")
                .build();

        doReturn(authResponse).when(authService).register(registerRequest);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + registerRequest.getEmail() + "\", \"password\": \"" + registerRequest.getPassword() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(authResponse.getToken()))
                .andExpect(jsonPath("$.tokenType").value(authResponse.getTokenType()))
                .andExpect(jsonPath("$.email").value(authResponse.getEmail()));
    }

    @Test
    @DisplayName("POST /api/login should return 200 OK and JWT token")
    void test_login_success_return200AndToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwtToken")
                .tokenType("Bearer")
                .email("user@example.com")
                .build();

        doReturn(authResponse).when(authService).login(loginRequest);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + loginRequest.getEmail() + "\", \"password\": \"" + loginRequest.getPassword() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(authResponse.getToken()))
                .andExpect(jsonPath("$.tokenType").value(authResponse.getTokenType()))
                .andExpect(jsonPath("$.email").value(authResponse.getEmail()));
    }
}
