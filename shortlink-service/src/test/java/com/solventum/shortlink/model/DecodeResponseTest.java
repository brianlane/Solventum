package com.solventum.shortlink.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DecodeResponse model.
 * 
 * Tests the response model functionality and data integrity.
 */
class DecodeResponseTest {
    
    @Test
    void testDecodeResponseConstructorWithParameters() {
        // Given
        String originalUrl = "https://example.com/library/react";
        String shortUrl = "http://short.est/abc123";
        
        // When
        DecodeResponse response = new DecodeResponse(originalUrl, shortUrl);
        
        // Then
        assertEquals(originalUrl, response.getOriginalUrl());
        assertEquals(shortUrl, response.getShortUrl());
        assertTrue(response.getTimestamp() > 0);
        assertTrue(response.getTimestamp() <= System.currentTimeMillis());
    }
    
    @Test
    void testDecodeResponseDefaultConstructor() {
        // Given
        DecodeResponse response = new DecodeResponse();
        
        // When
        response.setOriginalUrl("https://example.com/test");
        response.setShortUrl("http://short.est/xyz789");
        response.setTimestamp(12345L);
        
        // Then
        assertEquals("https://example.com/test", response.getOriginalUrl());
        assertEquals("http://short.est/xyz789", response.getShortUrl());
        assertEquals(12345L, response.getTimestamp());
    }
    
    @Test
    void testDecodeResponseTimestampIsCurrentTime() {
        // Given
        long beforeCreation = System.currentTimeMillis();
        
        // When
        DecodeResponse response = new DecodeResponse("long", "short");
        
        // Then
        long afterCreation = System.currentTimeMillis();
        assertTrue(response.getTimestamp() >= beforeCreation);
        assertTrue(response.getTimestamp() <= afterCreation);
    }
    
    @Test
    void testDecodeResponseToString() {
        // Given
        DecodeResponse response = new DecodeResponse("https://example.com", "http://short.est/abc");
        
        // When
        String toString = response.toString();
        
        // Then
        assertTrue(toString.contains("DecodeResponse"));
        assertTrue(toString.contains("https://example.com"));
        assertTrue(toString.contains("http://short.est/abc"));
        assertTrue(toString.contains(String.valueOf(response.getTimestamp())));
    }
    
    @Test
    void testDecodeResponseWithNullValues() {
        // Given
        DecodeResponse response = new DecodeResponse(null, null);
        
        // When & Then
        assertNull(response.getOriginalUrl());
        assertNull(response.getShortUrl());
        assertTrue(response.getTimestamp() > 0);
    }
    
    @Test
    void testDecodeResponseSettersAndGetters() {
        // Given
        DecodeResponse response = new DecodeResponse();
        String originalUrl = "https://example.com/test/path";
        String shortUrl = "http://short.est/test123";
        long timestamp = System.currentTimeMillis();
        
        // When
        response.setOriginalUrl(originalUrl);
        response.setShortUrl(shortUrl);
        response.setTimestamp(timestamp);
        
        // Then
        assertEquals(originalUrl, response.getOriginalUrl());
        assertEquals(shortUrl, response.getShortUrl());
        assertEquals(timestamp, response.getTimestamp());
    }
    
    @Test
    void testDecodeResponseTimestampPrecision() {
        // Test that timestamp is set with millisecond precision
        long timestamp1 = System.currentTimeMillis();
        DecodeResponse response1 = new DecodeResponse("long1", "short1");
        
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        DecodeResponse response2 = new DecodeResponse("long2", "short2");
        long timestamp2 = System.currentTimeMillis();
        
        // Then
        assertTrue(response1.getTimestamp() >= timestamp1);
        assertTrue(response2.getTimestamp() <= timestamp2);
        assertTrue(response2.getTimestamp() >= response1.getTimestamp());
    }
    
    @Test
    void testDecodeResponseWithLongUrls() {
        // Given
        StringBuilder longOriginalUrl = new StringBuilder("https://example.com");
        for (int i = 0; i < 100; i++) {
            longOriginalUrl.append("/segment").append(i);
        }
        String originalUrl = longOriginalUrl.toString();
        String shortUrl = "http://short.est/abc";
        
        // When
        DecodeResponse response = new DecodeResponse(originalUrl, shortUrl);
        
        // Then
        assertEquals(originalUrl, response.getOriginalUrl());
        assertEquals(shortUrl, response.getShortUrl());
        assertTrue(response.getTimestamp() > 0);
    }
    
    @Test
    void testDecodeResponseParameterOrder() {
        // Test that the constructor parameters are in the correct order
        // (originalUrl first, shortUrl second)
        String originalUrl = "https://example.com/original";
        String shortUrl = "http://short.est/short";
        
        // When
        DecodeResponse response = new DecodeResponse(originalUrl, shortUrl);
        
        // Then
        assertEquals(originalUrl, response.getOriginalUrl());
        assertEquals(shortUrl, response.getShortUrl());
    }
}