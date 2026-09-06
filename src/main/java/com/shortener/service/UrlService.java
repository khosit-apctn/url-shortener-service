package com.shortener.service;


import com.shortener.common.helpers.Base62Encoder;
import com.shortener.model.db.ShortUrl;
import com.shortener.model.db.User;
import com.shortener.model.request.ShortenRequest;
import com.shortener.model.response.ShortenResponse;
import com.shortener.repository.ShortUrlRepository;
import com.shortener.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;
    private final Base62Encoder base62Encoder;
    @Value("${app.base-url}")
    private String baseUrl;
    private static final int CODE_LENGTH = 6;

    @Transactional
    public ShortenResponse shortenUrl(ShortenRequest shortenRequest, String userEmail) {

        String shortCode = generateUniqueShortCode();
        User user = null;
        if (!userEmail.isEmpty()) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(shortenRequest.getOriginalUrl())
                .user(user)
                .isActive(true)
                .build();
        shortUrlRepository.save(shortUrl);
        String fullShortUrl = baseUrl.endsWith("/") ? baseUrl + shortCode : baseUrl + "/" + shortCode;
        return ShortenResponse.builder()
                .shortUrl(fullShortUrl)
                .build();
    }

    private String generateUniqueShortCode() {
        while (true) {
            String code = base62Encoder.generateRandomCode(CODE_LENGTH);
            if (!shortUrlRepository.existsByShortCode(code)) {
                return code;
            }
        }
    }
}