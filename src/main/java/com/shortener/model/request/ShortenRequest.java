package com.shortener.model.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.shortener.constants.enums.RegexPattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenRequest {

    @NotBlank(message = "original_url is required")
    @Pattern(
            regexp = RegexPattern.URL_REGEX,
            message = "Invalid URL format. Must be a valid HTTP or HTTPS URL"
    )
    @JsonProperty("original_url")
    private String originalUrl;
}

