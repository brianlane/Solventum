package com.solventum.shortlink.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request model for URL decoding endpoint.
 * 
 * Contains the short URL that needs to be decoded to its original form.
 */
public class DecodeRequest {
    
    @NotBlank(message = "Short URL is required")
    @Size(max = 255, message = "Short URL is too long")
    private String shortUrl;
    
    // Default constructor for Jackson
    public DecodeRequest() {
    }
    
    public DecodeRequest(String shortUrl) {
        this.shortUrl = shortUrl;
    }
    
    public String getShortUrl() {
        return shortUrl;
    }
    
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
    
    @Override
    public String toString() {
        return "DecodeRequest{" +
                "shortUrl='" + shortUrl + '\'' +
                '}';
    }
}