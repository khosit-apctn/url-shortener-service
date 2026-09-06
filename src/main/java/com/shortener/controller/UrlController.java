package com.shortener.controller;

import com.shortener.model.request.ShortenRequest;
import com.shortener.model.response.ShortenResponse;
import com.shortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(
            @Valid @RequestBody ShortenRequest shortenRequest,
            @AuthenticationPrincipal String userEmail
    ) {
        ShortenResponse response = urlService.shortenUrl(shortenRequest, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
