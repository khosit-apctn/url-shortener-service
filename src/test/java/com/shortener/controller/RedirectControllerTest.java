package com.shortener.controller;

import com.shortener.common.exception.DataNotFoundException;
import com.shortener.common.exception.GlobalExceptionHandler;
import com.shortener.model.response.ErrorResponse;
import com.shortener.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UrlService urlService;

    @InjectMocks
    private RedirectController redirectController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(redirectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /r/{shortCode} should redirect to original URL with 302 Found")
    void test_redirect_success_whenShortCodeExists() throws Exception {
        String shortCode = "shortCode";
        String targetUrl = "https://example.com/targetUrl";

        doReturn(targetUrl).when(urlService).getOriginalUrl(shortCode);

        mockMvc.perform(get("/r/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", targetUrl));
    }

    @Test
    @DisplayName("GET /r/{shortCode} should return 404 Not Found when code does not exist")
    void test_redirect_fail_whenShortCodeNotFound() throws Exception {
        String shortCode = "unknown";
        String errorMessage = "Short URL not found for code: " + shortCode;

        doThrow(new DataNotFoundException(errorMessage)).when(urlService).getOriginalUrl(shortCode);

        ErrorResponse expectedError = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message(errorMessage)
                .build();

        mockMvc.perform(get("/r/" + shortCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(expectedError.getStatus()))
                .andExpect(jsonPath("$.error").value(expectedError.getError()))
                .andExpect(jsonPath("$.message").value(expectedError.getMessage()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
