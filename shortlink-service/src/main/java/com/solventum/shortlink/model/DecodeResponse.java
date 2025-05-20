package com.solventum.shortlink.model;

/**
 * Response model for URL decoding endpoint.
 * 
 * Contains both the short URL and the decoded original URL.
 */
public class DecodeResponse {
    
    private String originalUrl;
    private String shortUrl;
    private long timestamp;
    
    // Default constructor for Jackson
    public DecodeResponse() {
    }
    
    public DecodeResponse(String originalUrl, String shortUrl) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getOriginalUrl() {
        return originalUrl;
    }
    
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    
    public String getShortUrl() {
        return shortUrl;
    }
    
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "DecodeResponse{" +
                "originalUrl='" + originalUrl + '\'' +
                ", shortUrl='" + shortUrl + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}