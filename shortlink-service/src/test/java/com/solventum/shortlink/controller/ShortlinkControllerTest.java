package com.solventum.shortlink.controller;

import com.solventum.shortlink.model.DecodeRequest;
import com.solventum.shortlink.model.EncodeRequest;
import com.solventum.shortlink.service.UrlShorteningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ShortlinkController.
 * 
 * Tests the REST endpoints with various scenarios including
 * valid requests, invalid inputs, and error conditions.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShortlinkControllerTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UrlShorteningService urlShorteningService;
        
    private String baseUrl;
    private HttpHeaders headers;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Clear any existing mappings for clean tests
        urlShorteningService.clearMappings();
    }
    
    @Test
    void testEncodeValidUrl() {
        // Given
        String longUrl = "https://example.com/library/react";
        EncodeRequest request = new EncodeRequest(longUrl);
        HttpEntity<EncodeRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/encode", HttpMethod.POST, entity, String.class);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("shortUrl"));
        assertTrue(response.getBody().contains("originalUrl"));
        assertTrue(response.getBody().contains(longUrl));
    }
    
    @Test
    void testEncodeInvalidUrl() {
        // Given
        String invalidUrl = "not-a-valid-url";
        EncodeRequest request = new EncodeRequest(invalidUrl);
        HttpEntity<EncodeRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/encode", HttpMethod.POST, entity, String.class);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    void testHealthEndpoint() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/health", String.class);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ShortLink service is running", response.getBody());
    }
    
    @Test
    void testStatsEndpoint() {
        // Given - Add some URLs to get stats
        urlShorteningService.encodeUrl("https://example1.com");
        urlShorteningService.encodeUrl("https://example2.com");
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/stats", String.class);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("totalUrls"));
        assertTrue(response.getBody().contains("timestamp"));
    }
    
    @Test
    void testDecodeValidShortUrl() {
        // Given - First encode a URL
        String longUrl = "https://example.com/library/react";
        String shortUrl = urlShorteningService.encodeUrl(longUrl);
        
        DecodeRequest request = new DecodeRequest(shortUrl);
        HttpEntity<DecodeRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/decode", HttpMethod.POST, entity, String.class);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("originalUrl"));
        assertTrue(response.getBody().contains("shortUrl"));
        assertTrue(response.getBody().contains(longUrl));
    }
}