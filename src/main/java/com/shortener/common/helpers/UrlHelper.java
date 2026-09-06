package com.shortener.common.helpers;

public final class UrlHelper {

    public static String buildFullShortUrl(String baseUrl, String shortCode) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return shortCode;
        }
        return baseUrl.endsWith("/") ? baseUrl + shortCode : baseUrl + "/" + shortCode;
    }
}
