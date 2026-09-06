package com.shortener.service;


import com.shortener.common.exception.DataNotFoundException;
import com.shortener.common.helpers.Base62Encoder;
import com.shortener.common.helpers.UrlHelper;
import com.shortener.model.db.ShortUrl;
import com.shortener.model.db.User;
import com.shortener.model.request.ShortenRequest;
import com.shortener.model.response.ShortenResponse;
import com.shortener.model.response.UrlResponse;
import com.shortener.repository.ShortUrlRepository;
import com.shortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        if (userEmail != null && !userEmail.isBlank()) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(shortenRequest.getOriginalUrl().trim())
                .user(user)
                .isActive(true)
                .build();
        shortUrlRepository.save(shortUrl);
        String fullShortUrl = UrlHelper.buildFullShortUrl(baseUrl, shortCode);
        return ShortenResponse.builder()
                .shortUrl(fullShortUrl)
                .build();
    }

    public String getOriginalUrl(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new DataNotFoundException("Short URL not found for code: " + shortCode));
        if (!shortUrl.isActive()) {
            throw new DataNotFoundException("This short URL has been deactivated");
        }
        return shortUrl.getOriginalUrl();
    }

    public List<UrlResponse> getUserUrls(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("User not found with email: " + userEmail));
        List<ShortUrl> urls = shortUrlRepository.findByUserIdOrderByCreatedTimestampDesc(user.getId());
        return urls.stream()
                .map(shortUrl -> {
                    String fullShortUrl = UrlHelper.buildFullShortUrl(baseUrl, shortUrl.getShortCode());
                    return UrlResponse.builder()
                            .shortUrl(fullShortUrl)
                            .originalUrl(shortUrl.getOriginalUrl())
                            .isActive(shortUrl.isActive())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateUrl(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("User not found with email: " + userEmail));
        ShortUrl shortUrl = shortUrlRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new DataNotFoundException("Short URL not found with id: " + id));
        shortUrl.setActive(false);
        shortUrlRepository.save(shortUrl);
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