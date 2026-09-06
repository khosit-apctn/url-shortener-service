package com.shortener.model.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShortenRequest {

    @NotBlank
    @JsonProperty("original_url")
    private String originalUrl;
}
