package com.shortener.common.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class Base62Encoder {

    private final String base62Chars;
    private final int base;
    private final SecureRandom random = new SecureRandom();

    public Base62Encoder(
            @Value("${app.base62-chars}")
            String base62Chars
    ) {
        this.base62Chars = base62Chars;
        this.base = base62Chars.length();
    }
    public String generateRandomCode(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(base62Chars.charAt(random.nextInt(base)));
        }
        return stringBuilder.toString();
    }
}
