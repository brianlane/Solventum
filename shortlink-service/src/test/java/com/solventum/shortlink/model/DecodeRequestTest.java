package com.solventum.shortlink.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DecodeRequest model.
 * 
 * Tests validation constraints and basic functionality.
 */
class DecodeRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidDecodeRequest() {
        // Given
        DecodeRequest request = new DecodeRequest("http://short.est/abc123");
        
        // When
        Set<ConstraintViolation<DecodeRequest>> violations = validator.validate(request);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals("http://short.est/abc123", request.getShortUrl());
    }
    
    @Test
    void testDecodeRequestWithNullShortUrl() {
        // Given
        DecodeRequest request = new DecodeRequest(null);
        
        // When
        Set<ConstraintViolation<DecodeRequest>> violations = validator.validate(request);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<DecodeRequest> violation = violations.iterator().next();
        assertEquals("Short URL is required", violation.getMessage());
        assertEquals("shortUrl", violation.getPropertyPath().toString());
    }
    
    @Test
    void testDecodeRequestWithEmptyShortUrl() {
        // Given
        DecodeRequest request = new DecodeRequest("");
        
        // When
        Set<ConstraintViolation<DecodeRequest>> violations = validator.validate(request);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<DecodeRequest> violation = violations.iterator().next();
        assertEquals("Short URL is required", violation.getMessage());
    }
    
    @Test
    void testDecodeRequestWithBlankShortUrl() {
        // Given
        DecodeRequest request = new DecodeRequest("   ");
        
        // When
        Set<ConstraintViolation<DecodeRequest>> violations = validator.validate(request);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<DecodeRequest> violation = violations.iterator().next();
        assertEquals("Short URL is required", violation.getMessage());
    }
    
    @Test
    void testDecodeRequestWithTooLongShortUrl() {
        // Given
        StringBuilder longShortUrl = new StringBuilder("http://short.est/");
        for (int i = 0; i < 300; i++) {
            longShortUrl.append("a");
        }
        DecodeRequest request = new DecodeRequest(longShortUrl.toString());
        
        // When
        Set<ConstraintViolation<DecodeRequest>> violations = validator.validate(request);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<DecodeRequest> violation = violations.iterator().next();
        assertEquals("Short URL is too long", violation.getMessage());
    }
    
    @Test
    void testDecodeRequestDefaultConstructor() {
        // Given
        DecodeRequest request = new DecodeRequest();
        
        // When
        request.setShortUrl("http://short.est/test");
        
        // Then
        assertEquals("http://short.est/test", request.getShortUrl());
    }
    
    @Test
    void testDecodeRequestToString() {
        // Given
        DecodeRequest request = new DecodeRequest("http://short.est/example");
        
        // When
        String toString = request.toString();
        
        // Then
        assertTrue(toString.contains("DecodeRequest"));
        assertTrue(toString.contains("http://short.est/example"));
    }
    
    @Test
    void testDecodeRequestEquality() {
        // Given
        DecodeRequest request1 = new DecodeRequest("http://short.est/same");
        DecodeRequest request2 = new DecodeRequest("http://short.est/same");
        DecodeRequest request3 = new DecodeRequest("http://short.est/different");
        
        // Then
        assertEquals(request1.getShortUrl(), request2.getShortUrl());
        assertNotEquals(request1.getShortUrl(), request3.getShortUrl());
    }
    
    @Test
    void testDecodeRequestMaxLengthBoundary() {
        // Given - Create short URL exactly at the boundary (255 characters)
        StringBuilder urlBuilder = new StringBuilder("http://short.est/");
        while (urlBuilder.length() < 255) {
            urlBuilder.append("a");
        }
        // Trim to exactly 255
        String shortUrl = urlBuilder.substring(0, 255);
        DecodeRequest request = new DecodeRequest(shortUrl);
        
        // When
        Set<ConstraintViolation<DecodeRequest>> violations = validator.validate(request);
        
        // Then
        assertTrue(violations.isEmpty(), "Short URL with exactly 255 characters should be valid");
        assertEquals(shortUrl, request.getShortUrl());
    }
    
    @Test
    void testDecodeRequestWithJustShortCode() {
        // Given
        DecodeRequest request = new DecodeRequest("abc123");
        
        // When
        Set<ConstraintViolation<DecodeRequest>> violations = validator.validate(request);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals("abc123", request.getShortUrl());
    }
    
    @Test
    void testDecodeRequestWithDifferentProtocols() {
        // Given
        String[] validShortUrls = {
            "http://short.est/abc123",
            "https://short.est/abc123",
            "abc123" // Just the code
        };
        
        for (String shortUrl : validShortUrls) {
            // When
            DecodeRequest request = new DecodeRequest(shortUrl);
            Set<ConstraintViolation<DecodeRequest>> violations = validator.validate(request);
            
            // Then
            assertTrue(violations.isEmpty(), "Should accept short URL: " + shortUrl);
            assertEquals(shortUrl, request.getShortUrl());
        }
    }
}