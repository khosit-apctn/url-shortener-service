package com.shortener.service;

import com.shortener.common.exception.DataNotFoundException;
import com.shortener.common.helpers.Base62Encoder;
import com.shortener.model.db.ShortUrl;
import com.shortener.model.db.User;
import com.shortener.model.request.ShortenRequest;
import com.shortener.model.response.ShortenResponse;
import com.shortener.model.response.UrlResponse;
import com.shortener.repository.ShortUrlRepository;
import com.shortener.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Base62Encoder base62Encoder;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080/r/");
    }

    @Test
    @DisplayName("Should shorten URL successfully for authenticated user")
    void test_shortenUrl_success_withAuthenticatedUser() {
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).build();
        ShortenRequest request = ShortenRequest.builder()
                .originalUrl("https://example.com/originalUrl")
                .build();

        ShortUrl expectedSavedUrl = ShortUrl.builder()
                .shortCode("shortCode")
                .originalUrl("https://example.com/originalUrl")
                .user(user)
                .isActive(true)
                .build();

        doReturn("shortCode").when(base62Encoder).generateRandomCode(6);
        doReturn(false).when(shortUrlRepository).existsByShortCode("shortCode");
        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(expectedSavedUrl).when(shortUrlRepository).save(expectedSavedUrl);

        ShortenResponse response = urlService.shortenUrl(request, email);

        ShortenResponse expectedResponse = ShortenResponse.builder()
                .shortUrl("http://localhost:8080/r/shortCode")
                .build();

        assertThat(response).isEqualTo(expectedResponse);
        verify(userRepository, times(1)).findByEmail(email);
        verify(shortUrlRepository, times(1)).save(expectedSavedUrl);
    }

    @Test
    @DisplayName("Should shorten URL successfully for anonymous user without NPE")
    void test_shortenUrl_success_withAnonymousUser() {
        ShortenRequest request = ShortenRequest.builder()
                .originalUrl("https://example.com/originalUrl")
                .build();

        ShortUrl expectedSavedUrl = ShortUrl.builder()
                .shortCode("shortCode")
                .originalUrl("https://example.com/originalUrl")
                .user(null)
                .isActive(true)
                .build();

        doReturn("shortCode").when(base62Encoder).generateRandomCode(6);
        doReturn(false).when(shortUrlRepository).existsByShortCode("shortCode");
        doReturn(expectedSavedUrl).when(shortUrlRepository).save(expectedSavedUrl);

        ShortenResponse response = urlService.shortenUrl(request, null);

        ShortenResponse expectedResponse = ShortenResponse.builder()
                .shortUrl("http://localhost:8080/r/shortCode")
                .build();

        assertThat(response).isEqualTo(expectedResponse);
        verifyNoInteractions(userRepository);
        verify(shortUrlRepository, times(1)).save(expectedSavedUrl);
    }

    @Test
    @DisplayName("Should return original URL when short code exists and is active")
    void test_getOriginalUrl_success_whenActive() {
        String shortCode = "shortCode";
        ShortUrl shortUrl = ShortUrl.builder()
                .id(1L)
                .shortCode(shortCode)
                .originalUrl("https://example.com")
                .isActive(true)
                .build();

        doReturn(Optional.of(shortUrl)).when(shortUrlRepository).findByShortCode(shortCode);

        String result = urlService.getOriginalUrl(shortCode);
        assertThat(result).isEqualTo(shortUrl.getOriginalUrl());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when short code not found")
    void test_getOriginalUrl_fail_whenNotFound() {
        String shortCode = "unknown";
        doReturn(Optional.empty()).when(shortUrlRepository).findByShortCode(shortCode);

        assertThatThrownBy(() -> urlService.getOriginalUrl(shortCode))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Short URL not found for code: " + shortCode);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when short URL is deactivated")
    void test_getOriginalUrl_fail_whenDeactivated() {
        String shortCode = "inactive";
        ShortUrl shortUrl = ShortUrl.builder()
                .id(1L)
                .shortCode(shortCode)
                .originalUrl("https://example.com")
                .isActive(false)
                .build();

        doReturn(Optional.of(shortUrl)).when(shortUrlRepository).findByShortCode(shortCode);

        assertThatThrownBy(() -> urlService.getOriginalUrl(shortCode))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("This short URL has been deactivated");
    }

    @Test
    @DisplayName("Should get user's URLs successfully")
    void test_getUserUrls_success_returnUrlList() {
        String email = "user@example.com";
        User user = User.builder().id(10L).email(email).build();
        ShortUrl url1 = ShortUrl.builder()
                .id(1L)
                .shortCode("code1")
                .originalUrl("https://site1.com")
                .isActive(true)
                .createdTimestamp(OffsetDateTime.now())
                .build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(List.of(url1)).when(shortUrlRepository).findByUserIdOrderByCreatedTimestampDesc(10L);

        List<UrlResponse> responses = urlService.getUserUrls(email);

        UrlResponse expectedResponse = UrlResponse.builder()
                .id(1L)
                .shortCode("code1")
                .shortUrl("http://localhost:8080/r/code1")
                .originalUrl("https://site1.com")
                .isActive(true)
                .build();

        assertThat(responses).isEqualTo(List.of(expectedResponse));
        verify(userRepository, times(1)).findByEmail(email);
        verify(shortUrlRepository, times(1)).findByUserIdOrderByCreatedTimestampDesc(10L);
    }

    @Test
    @DisplayName("Should deactivate URL successfully for owner")
    void test_deactivateUrl_success_ownerDeactivates() {
        String email = "user@example.com";
        User user = User.builder().id(10L).email(email).build();
        ShortUrl url = ShortUrl.builder()
                .id(1L)
                .shortCode("code1")
                .originalUrl("https://site1.com")
                .isActive(true)
                .build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(url)).when(shortUrlRepository).findByIdAndUserId(1L, 10L);
        doReturn(url).when(shortUrlRepository).save(url);

        urlService.deactivateUrl(1L, email);

        assertThat(url.isActive()).isFalse();
        verify(shortUrlRepository, times(1)).save(url);
    }
}
