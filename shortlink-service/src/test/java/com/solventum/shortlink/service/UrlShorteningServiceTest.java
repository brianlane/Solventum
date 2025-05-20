package com.solventum.shortlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UrlShorteningService.
 * 
 * Tests the core functionality of URL encoding and decoding,
 * edge cases, and error scenarios.
 */
class UrlShorteningServiceTest {
    
    private UrlShorteningService urlShorteningService;
    
    @BeforeEach
    void setUp() {
        urlShorteningService = new UrlShorteningService();
        // Set the base URL using reflection since it's normally injected by Spring
        ReflectionTestUtils.setField(urlShorteningService, "baseUrl", "http://short.est/");
    }
    
    @Test
    void testEncodeValidUrl() {
        // Given
        String longUrl = "https://example.com/library/react";
        
        // When
        String shortUrl = urlShorteningService.encodeUrl(longUrl);
        
        // Then
        assertNotNull(shortUrl);
        assertTrue(shortUrl.startsWith("http://short.est/"));
        assertTrue(shortUrl.length() > "http://short.est/".length());
    }
    
    @Test
    void testDecodeValidShortUrl() {
        // Given
        String longUrl = "https://example.com/library/react";
        String shortUrl = urlShorteningService.encodeUrl(longUrl);
        
        // When
        String decodedUrl = urlShorteningService.decodeUrl(shortUrl);
        
        // Then
        assertEquals(longUrl, decodedUrl);
    }
    
    @Test
    void testEncodeDecodeRoundTrip() {
        // Given
        String[] testUrls = {
            "https://www.google.com",
            "http://example.com/path/to/resource",
            "https://subdomain.domain.com/complex/path?param=value&other=123",
            "https://very-long-domain-name.com/very/long/path/with/many/segments"
        };
        
        for (String originalUrl : testUrls) {
            // When
            String shortUrl = urlShorteningService.encodeUrl(originalUrl);
            String decodedUrl = urlShorteningService.decodeUrl(shortUrl);
            
            // Then
            assertEquals(originalUrl, decodedUrl, 
                "Round trip failed for URL: " + originalUrl);
        }
    }
    
    @Test
    void testEncodeNullUrl() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> urlShorteningService.encodeUrl(null));
        
        assertEquals("URL cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testEncodeEmptyUrl() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> urlShorteningService.encodeUrl(""));
        assertThrows(IllegalArgumentException.class, 
            () -> urlShorteningService.encodeUrl("   "));
    }
    
    @Test
    void testEncodeInvalidUrl() {
        // Given
        String[] invalidUrls = {
            "not-a-url",
            "www.example.com", // Missing protocol
            "https://", // Incomplete
            "https://domain", // Missing TLD
            "ftp://example.com" // Wrong protocol
        };
        
        for (String invalidUrl : invalidUrls) {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> urlShorteningService.encodeUrl(invalidUrl),
                "Should reject invalid URL: " + invalidUrl);
            
            assertEquals("Invalid URL format", exception.getMessage());
        }
    }
    
    @Test
    void testDecodeNullShortUrl() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> urlShorteningService.decodeUrl(null));
        
        assertEquals("Short URL cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testDecodeEmptyShortUrl() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> urlShorteningService.decodeUrl(""));
        assertThrows(IllegalArgumentException.class, 
            () -> urlShorteningService.decodeUrl("   "));
    }
    
    @Test
    void testDecodeNonExistentShortUrl() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> urlShorteningService.decodeUrl("http://short.est/nonexistent"));
        
        assertEquals("Short URL not found", exception.getMessage());
    }
    
    @Test
    void testSameUrlReturnsSameShortUrl() {
        // Given
        String longUrl = "https://example.com/same-url";
        
        // When
        String shortUrl1 = urlShorteningService.encodeUrl(longUrl);
        String shortUrl2 = urlShorteningService.encodeUrl(longUrl);
        
        // Then
        assertEquals(shortUrl1, shortUrl2);
    }
    
    @Test
    void testDifferentUrlsReturnDifferentShortUrls() {
        // Given
        String longUrl1 = "https://example1.com";
        String longUrl2 = "https://example2.com";
        
        // When
        String shortUrl1 = urlShorteningService.encodeUrl(longUrl1);
        String shortUrl2 = urlShorteningService.encodeUrl(longUrl2);
        
        // Then
        assertNotEquals(shortUrl1, shortUrl2);
    }
    
    @Test
    void testUrlTrimming() {
        // Given
        String longUrl = "https://example.com/trimmed";
        String longUrlWithSpaces = "  " + longUrl + "  ";
        
        // When
        String shortUrl1 = urlShorteningService.encodeUrl(longUrl);
        String shortUrl2 = urlShorteningService.encodeUrl(longUrlWithSpaces);
        
        // Then
        assertEquals(shortUrl1, shortUrl2);
    }
    
    @Test
    void testDecodeWithDifferentFormats() {
        // Given
        String longUrl = "https://example.com/format-test";
        String shortUrl = urlShorteningService.encodeUrl(longUrl);
        String shortCode = shortUrl.substring("http://short.est/".length());
        
        // When & Then
        assertEquals(longUrl, urlShorteningService.decodeUrl(shortUrl));
        assertEquals(longUrl, urlShorteningService.decodeUrl(shortCode));
        assertEquals(longUrl, urlShorteningService.decodeUrl("  " + shortUrl + "  "));
    }
    
    @Test
    void testGetUrlMappingSize() {
        // Given
        int initialSize = urlShorteningService.getUrlMappingSize();
        
        // When
        urlShorteningService.encodeUrl("https://example1.com");
        urlShorteningService.encodeUrl("https://example2.com");
        urlShorteningService.encodeUrl("https://example1.com"); // Duplicate
        
        // Then
        assertEquals(initialSize + 2, urlShorteningService.getUrlMappingSize());
    }
    
    @Test
    void testClearMappings() {
        // Given
        urlShorteningService.encodeUrl("https://example1.com");
        urlShorteningService.encodeUrl("https://example2.com");
        assertTrue(urlShorteningService.getUrlMappingSize() > 0);
        
        // When
        urlShorteningService.clearMappings();
        
        // Then
        assertEquals(0, urlShorteningService.getUrlMappingSize());
        
        // Verify that previously encoded URLs no longer work
        assertThrows(IllegalArgumentException.class, 
            () -> urlShorteningService.decodeUrl("http://short.est/a"));
    }
    
    @Test
    void testBase62Encoding() {
        // Test that we get valid Base-62 characters in our short URLs
        String longUrl = "https://example.com/base62-test";
        String shortUrl = urlShorteningService.encodeUrl(longUrl);
        String shortCode = shortUrl.substring("http://short.est/".length());
        
        // Verify only valid Base-62 characters
        assertTrue(shortCode.matches("[a-zA-Z0-9]+"), 
            "Short code should only contain Base-62 characters: " + shortCode);
    }
    
    @Test
    void testSequentialGeneration() {
        // Test that sequential URLs generate different short codes
        String[] shortUrls = new String[10];
        for (int i = 0; i < 10; i++) {
            shortUrls[i] = urlShorteningService.encodeUrl("https://example" + i + ".com");
        }
        
        // Verify all are unique
        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                assertNotEquals(shortUrls[i], shortUrls[j], 
                    "URLs " + i + " and " + j + " should be different");
            }
        }
    }
    
    @Test
    void testDecodeInvalidShortCode() {
        // Given - Try to decode a short URL with invalid format
        String invalidShortUrl = "http://different.domain/abc123";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> urlShorteningService.decodeUrl(invalidShortUrl));
        
        assertEquals("Short URL not found", exception.getMessage());
    }
    
    @Test
    void testConcurrentAccess() {
        // Test thread safety with multiple threads encoding URLs
        int numThreads = 10;
        int urlsPerThread = 50;
        Thread[] threads = new Thread[numThreads];
        
        // Create threads that will encode URLs concurrently
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < urlsPerThread; j++) {
                    String url = "https://example" + threadId + "-" + j + ".com";
                    String shortUrl = urlShorteningService.encodeUrl(url);
                    assertNotNull(shortUrl);
                    
                    // Verify we can decode it back
                    String decoded = urlShorteningService.decodeUrl(shortUrl);
                    assertEquals(url, decoded);
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted");
            }
        }
        
        // Verify total count
        assertEquals(numThreads * urlsPerThread, urlShorteningService.getUrlMappingSize());
    }
    
    @Test
    void testLongUrlSupport() {
        // Test encoding and decoding of very long URLs
        StringBuilder longUrlBuilder = new StringBuilder("https://example.com");
        longUrlBuilder.append("/very/long/path");
        for (int i = 0; i < 100; i++) {
            longUrlBuilder.append("/segment").append(i);
        }
        longUrlBuilder.append("?param1=value1&param2=value2");
        
        String longUrl = longUrlBuilder.toString();
        
        // When
        String shortUrl = urlShorteningService.encodeUrl(longUrl);
        String decodedUrl = urlShorteningService.decodeUrl(shortUrl);
        
        // Then
        assertEquals(longUrl, decodedUrl);
        assertTrue(shortUrl.length() < longUrl.length());
    }
}