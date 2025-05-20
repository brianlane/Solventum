package com.solventum.shortlink.model;

/**
 * Response model for URL encoding endpoint.
 * 
 * Contains both the original URL and the generated short URL.
 */
public class EncodeResponse {
    
    private String shortUrl;
    private String originalUrl;
    private long timestamp;
    
    // Default constructor for Jackson
    public EncodeResponse() {
    }
    
    public EncodeResponse(String shortUrl, String originalUrl) {
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getShortUrl() {
        return shortUrl;
    }
    
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
    
    public String getOriginalUrl() {
        return originalUrl;
    }
    
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "EncodeResponse{" +
                "shortUrl='" + shortUrl + '\'' +
                ", originalUrl='" + originalUrl + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}