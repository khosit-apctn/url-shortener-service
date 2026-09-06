package com.shortener.controller;

import com.shortener.common.exception.GlobalExceptionHandler;
import com.shortener.model.request.ShortenRequest;
import com.shortener.model.response.ShortenResponse;
import com.shortener.model.response.UrlResponse;
import com.shortener.service.UrlService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UrlService urlService;

    @InjectMocks
    private UrlController urlController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(urlController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(localValidatorFactoryBean)
                .build();
    }

    @Test
    @DisplayName("POST /api/shorten with valid URL should return 201 Created")
    void test_shortenUrl_success_validUrl() throws Exception {
        ShortenRequest shortenRequest = ShortenRequest.builder()
                .originalUrl("https://example.com/originalUrl")
                .build();

        ShortenResponse shortenResponse = ShortenResponse.builder()
                .shortUrl("http://localhost:8080/r/shortUrl")
                .build();

        doReturn(shortenResponse).when(urlService).shortenUrl(shortenRequest, null);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"original_url\": \"" + shortenRequest.getOriginalUrl() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").value(shortenResponse.getShortUrl()));
    }

    @Test
    @DisplayName("POST /api/shorten with invalid URL format should return 400 Bad Request")
    void test_shortenUrl_fail_invalidUrl() throws Exception {
        ShortenRequest shortenRequest = ShortenRequest.builder()
                .originalUrl("invalid-originalUrl")
                .build();

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"original_url\": \"" + shortenRequest.getOriginalUrl() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andExpect(jsonPath("$.details[0]").value("Invalid URL format. Must be a valid HTTP or HTTPS URL"));
    }

    @Test
    @DisplayName("GET /api/urls should return 200 OK with user URLs")
    void test_getUserUrls_success_returnUserUrls() throws Exception {
        UrlResponse urlResponse1 = UrlResponse.builder()
                .id(1L)
                .shortCode("shortCode1")
                .shortUrl("http://localhost:8080/r/shortCode1")
                .originalUrl("https://example.com/originalUrl1")
                .isActive(true)
                .build();

        UrlResponse urlResponse2 = UrlResponse.builder()
                .id(2L)
                .shortCode("shortCode2")
                .shortUrl("http://localhost:8080/r/shortCode2")
                .originalUrl("https://example.com/originalUrl2")
                .isActive(false)
                .build();

        List<UrlResponse> expectedResponses = List.of(urlResponse1, urlResponse2);

        doReturn(expectedResponses).when(urlService).getUserUrls(null);

        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(urlResponse1.getId()))
                .andExpect(jsonPath("$[0].shortCode").value(urlResponse1.getShortCode()))
                .andExpect(jsonPath("$[0].shortUrl").value(urlResponse1.getShortUrl()))
                .andExpect(jsonPath("$[0].originalUrl").value(urlResponse1.getOriginalUrl()))
                .andExpect(jsonPath("$[0].isActive").value(urlResponse1.getIsActive()))
                .andExpect(jsonPath("$[1].id").value(urlResponse2.getId()))
                .andExpect(jsonPath("$[1].shortCode").value(urlResponse2.getShortCode()))
                .andExpect(jsonPath("$[1].shortUrl").value(urlResponse2.getShortUrl()))
                .andExpect(jsonPath("$[1].originalUrl").value(urlResponse2.getOriginalUrl()))
                .andExpect(jsonPath("$[1].isActive").value(urlResponse2.getIsActive()));
    }

    @Test
    @DisplayName("DELETE /api/urls/{id} should return 204 No Content")
    void test_deleteUrl_success_return204() throws Exception {
        Long targetId = 1L;
        doNothing().when(urlService).deactivateUrl(targetId, null);

        mockMvc.perform(delete("/api/urls/" + targetId))
                .andExpect(status().isNoContent());

        verify(urlService, times(1)).deactivateUrl(targetId, null);
    }
}
