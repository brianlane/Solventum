package com.solventum.shortlink.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request model for URL encoding endpoint.
 * 
 * Contains the long URL that needs to be shortened.
 */
public class EncodeRequest {
    
    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL is too long (maximum 2048 characters)")
    private String url;
    
    // Default constructor for Jackson
    public EncodeRequest() {
    }
    
    public EncodeRequest(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    @Override
    public String toString() {
        return "EncodeRequest{" +
                "url='" + url + '\'' +
                '}';
    }
}