package com.solventum.shortlink.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EncodeResponse model.
 * 
 * Tests the response model functionality and data integrity.
 */
class EncodeResponseTest {
    
    @Test
    void testEncodeResponseConstructorWithParameters() {
        // Given
        String shortUrl = "http://short.est/abc123";
        String originalUrl = "https://example.com/library/react";
        
        // When
        EncodeResponse response = new EncodeResponse(shortUrl, originalUrl);
        
        // Then
        assertEquals(shortUrl, response.getShortUrl());
        assertEquals(originalUrl, response.getOriginalUrl());
        assertTrue(response.getTimestamp() > 0);
        assertTrue(response.getTimestamp() <= System.currentTimeMillis());
    }
    
    @Test
    void testEncodeResponseDefaultConstructor() {
        // Given
        EncodeResponse response = new EncodeResponse();
        
        // When
        response.setShortUrl("http://short.est/xyz789");
        response.setOriginalUrl("https://example.com/test");
        response.setTimestamp(12345L);
        
        // Then
        assertEquals("http://short.est/xyz789", response.getShortUrl());
        assertEquals("https://example.com/test", response.getOriginalUrl());
        assertEquals(12345L, response.getTimestamp());
    }
    
    @Test
    void testEncodeResponseTimestampIsCurrentTime() {
        // Given
        long beforeCreation = System.currentTimeMillis();
        
        // When
        EncodeResponse response = new EncodeResponse("short", "long");
        
        // Then
        long afterCreation = System.currentTimeMillis();
        assertTrue(response.getTimestamp() >= beforeCreation);
        assertTrue(response.getTimestamp() <= afterCreation);
    }
    
    @Test
    void testEncodeResponseToString() {
        // Given
        EncodeResponse response = new EncodeResponse("http://short.est/abc", "https://example.com");
        
        // When
        String toString = response.toString();
        
        // Then
        assertTrue(toString.contains("EncodeResponse"));
        assertTrue(toString.contains("http://short.est/abc"));
        assertTrue(toString.contains("https://example.com"));
        assertTrue(toString.contains(String.valueOf(response.getTimestamp())));
    }
    
    @Test
    void testEncodeResponseWithNullValues() {
        // Given
        EncodeResponse response = new EncodeResponse(null, null);
        
        // When & Then
        assertNull(response.getShortUrl());
        assertNull(response.getOriginalUrl());
        assertTrue(response.getTimestamp() > 0);
    }
    
    @Test
    void testEncodeResponseSettersAndGetters() {
        // Given
        EncodeResponse response = new EncodeResponse();
        String shortUrl = "http://short.est/test123";
        String originalUrl = "https://example.com/test/path";
        long timestamp = System.currentTimeMillis();
        
        // When
        response.setShortUrl(shortUrl);
        response.setOriginalUrl(originalUrl);
        response.setTimestamp(timestamp);
        
        // Then
        assertEquals(shortUrl, response.getShortUrl());
        assertEquals(originalUrl, response.getOriginalUrl());
        assertEquals(timestamp, response.getTimestamp());
    }
    
    @Test
    void testEncodeResponseTimestampPrecision() {
        // Test that timestamp is set with millisecond precision
        long timestamp1 = System.currentTimeMillis();
        EncodeResponse response1 = new EncodeResponse("short1", "long1");
        
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        EncodeResponse response2 = new EncodeResponse("short2", "long2");
        long timestamp2 = System.currentTimeMillis();
        
        // Then
        assertTrue(response1.getTimestamp() >= timestamp1);
        assertTrue(response2.getTimestamp() <= timestamp2);
        assertTrue(response2.getTimestamp() >= response1.getTimestamp());
    }
}