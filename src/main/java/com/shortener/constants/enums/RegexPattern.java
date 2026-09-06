package com.shortener.constants.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegexPattern {
    URL;
    public static final String URL_REGEX = "^https?://(localhost|([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})|(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}))(:[0-9]+)?(/.*)?$";
}
