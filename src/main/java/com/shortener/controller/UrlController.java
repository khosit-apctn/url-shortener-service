package com.shortener.controller;

import com.shortener.model.request.ShortenRequest;
import com.shortener.model.response.ShortenResponse;
import com.shortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.shortener.model.response.UrlResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/urls")
    public ResponseEntity<List<UrlResponse>> getUserUrls(@AuthenticationPrincipal String userEmail) {
        List<UrlResponse> urls = urlService.getUserUrls(userEmail);
        return ResponseEntity.ok(urls);
    }

    @DeleteMapping("/urls/{id}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal String userEmail
    ) {
        urlService.deactivateUrl(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}
