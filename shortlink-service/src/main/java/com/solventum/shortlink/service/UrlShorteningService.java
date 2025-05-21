package com.solventum.shortlink.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Service responsible for URL encoding and decoding operations.
 * 
 * This service implements a Base-62 encoding algorithm to generate short URLs
 * from long URLs and maintains bidirectional mapping for quick lookups.
 * 
 * Thread-safe implementation using ConcurrentHashMap and AtomicLong.
 */
@Service
public class UrlShorteningService {
    
    // Base-62 alphabet: a-z, A-Z, 0-9 (62 characters total)
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length();
    
    // URL validation pattern - basic but effective for most cases
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://.+\\..+$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Thread-safe storage for bidirectional URL mapping
    private final ConcurrentHashMap<String, String> shortToLongMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> longToShortMap = new ConcurrentHashMap<>();
    
    // Atomic counter to ensure unique IDs
    private final AtomicLong counter = new AtomicLong(1);
    
    // Configuration for short URL base
    @Value("${app.short-url.base-url:http://short.est/}")
    private String baseUrl;
    
    /**
     * Encodes a long URL into a short URL.
     * 
     * @param longUrl the original URL to be shortened
     * @return the shortened URL
     * @throws IllegalArgumentException if the URL is invalid or null
     */
    public String encodeUrl(String longUrl) {
        // Input validation
        if (longUrl == null || longUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        
        // Normalize URL (trim whitespace)
        longUrl = longUrl.trim();
        
        // Validate URL format
        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("Invalid URL format");
        }
        
        // Check if URL already exists in our mapping
        String existingShortCode = longToShortMap.get(longUrl);
        if (existingShortCode != null) {
            return baseUrl + existingShortCode;
        }
        
        // Generate new short code
        long id = counter.getAndIncrement();
        String shortCode = encodeBase62(id);
        
        // Store bidirectional mapping
        shortToLongMap.put(shortCode, longUrl);
        longToShortMap.put(longUrl, shortCode);
        
        return baseUrl + shortCode;
    }
    
    /**
     * Decodes a short URL back to its original long URL.
     * 
     * @param shortUrl the shortened URL to be decoded
     * @return the original long URL
     * @throws IllegalArgumentException if the short URL is invalid or not found
     */
    public String decodeUrl(String shortUrl) {
        // Input validation
        if (shortUrl == null || shortUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Short URL cannot be null or empty");
        }
        
        // Normalize input
        shortUrl = shortUrl.trim();
        
        // Extract short code from URL
        String shortCode = extractShortCode(shortUrl);
        if (shortCode == null) {
            throw new IllegalArgumentException("Invalid short URL format");
        }
        
        // Look up original URL
        String longUrl = shortToLongMap.get(shortCode);
        if (longUrl == null) {
            throw new IllegalArgumentException("Short URL not found");
        }
        
        return longUrl;
    }
    
    /**
     * Encodes a number into Base-62 representation.
     * 
     * @param num the number to encode
     * @return Base-62 encoded string
     */
    private String encodeBase62(long num) {
        if (num == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        
        StringBuilder encoded = new StringBuilder();
        while (num > 0) {
            encoded.append(ALPHABET.charAt((int) (num % BASE)));
            num /= BASE;
        }
        
        return encoded.reverse().toString();
    }
    
    /**
     * Validates if a string is a properly formatted URL.
     * 
     * @param url the URL string to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }
    
    /**
     * Extracts the short code from a short URL.
     * 
     * @param shortUrl the short URL
     * @return the extracted short code, or null if extraction fails
     */
    private String extractShortCode(String shortUrl) {
        // Handle both full URLs and just the short codes
        if (shortUrl.startsWith(baseUrl)) {
            return shortUrl.substring(baseUrl.length());
        } else if (shortUrl.startsWith("http://") || shortUrl.startsWith("https://")) {
            // Different base URL format
            int lastSlashIndex = shortUrl.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < shortUrl.length() - 1) {
                return shortUrl.substring(lastSlashIndex + 1);
            }
        } else {
            // Assume it's just the short code
            return shortUrl;
        }
        
        return null;
    }
    
    /**
     * Gets the current size of the URL mapping.
     * Useful for monitoring and testing.
     * 
     * @return the number of stored URL mappings
     */
    public int getUrlMappingSize() {
        return shortToLongMap.size();
    }
    
    /**
     * Clears all URL mappings.
     * Primarily used for testing purposes.
     */
    public void clearMappings() {
        shortToLongMap.clear();
        longToShortMap.clear();
        // Note: We don't reset the counter to maintain uniqueness
    }
}