package com.shortener.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortenResponse {

    @JsonProperty("short_url")
    private String shortUrl;
}