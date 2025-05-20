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
 * Unit tests for EncodeRequest model.
 * 
 * Tests validation constraints and basic functionality.
 */
class EncodeRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidEncodeRequest() {
        // Given
        EncodeRequest request = new EncodeRequest("https://example.com");
        
        // When
        Set<ConstraintViolation<EncodeRequest>> violations = validator.validate(request);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals("https://example.com", request.getUrl());
    }
    
    @Test
    void testEncodeRequestWithNullUrl() {
        // Given
        EncodeRequest request = new EncodeRequest(null);
        
        // When
        Set<ConstraintViolation<EncodeRequest>> violations = validator.validate(request);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<EncodeRequest> violation = violations.iterator().next();
        assertEquals("URL is required", violation.getMessage());
        assertEquals("url", violation.getPropertyPath().toString());
    }
    
    @Test
    void testEncodeRequestWithEmptyUrl() {
        // Given
        EncodeRequest request = new EncodeRequest("");
        
        // When
        Set<ConstraintViolation<EncodeRequest>> violations = validator.validate(request);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<EncodeRequest> violation = violations.iterator().next();
        assertEquals("URL is required", violation.getMessage());
    }
    
    @Test
    void testEncodeRequestWithBlankUrl() {
        // Given
        EncodeRequest request = new EncodeRequest("   ");
        
        // When
        Set<ConstraintViolation<EncodeRequest>> violations = validator.validate(request);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<EncodeRequest> violation = violations.iterator().next();
        assertEquals("URL is required", violation.getMessage());
    }
    
    @Test
    void testEncodeRequestWithTooLongUrl() {
        // Given
        StringBuilder longUrl = new StringBuilder("https://example.com");
        for (int i = 0; i < 2048; i++) {
            longUrl.append("a");
        }
        EncodeRequest request = new EncodeRequest(longUrl.toString());
        
        // When
        Set<ConstraintViolation<EncodeRequest>> violations = validator.validate(request);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<EncodeRequest> violation = violations.iterator().next();
        assertEquals("URL is too long (maximum 2048 characters)", violation.getMessage());
    }
    
    @Test
    void testEncodeRequestDefaultConstructor() {
        // Given
        EncodeRequest request = new EncodeRequest();
        
        // When
        request.setUrl("https://example.com");
        
        // Then
        assertEquals("https://example.com", request.getUrl());
    }
    
    @Test
    void testEncodeRequestToString() {
        // Given
        EncodeRequest request = new EncodeRequest("https://example.com");
        
        // When
        String toString = request.toString();
        
        // Then
        assertTrue(toString.contains("EncodeRequest"));
        assertTrue(toString.contains("https://example.com"));
    }
    
    @Test
    void testEncodeRequestEquality() {
        // Given
        EncodeRequest request1 = new EncodeRequest("https://example.com");
        EncodeRequest request2 = new EncodeRequest("https://example.com");
        EncodeRequest request3 = new EncodeRequest("https://different.com");
        
        // Then
        assertEquals(request1.getUrl(), request2.getUrl());
        assertNotEquals(request1.getUrl(), request3.getUrl());
    }
    
    @Test
    void testEncodeRequestMaxLengthBoundary() {
        // Given - Create URL exactly at the boundary (2048 characters)
        StringBuilder urlBuilder = new StringBuilder("https://example.com");
        while (urlBuilder.length() < 2048) {
            urlBuilder.append("a");
        }
        // Trim to exactly 2048
        String url = urlBuilder.substring(0, 2048);
        EncodeRequest request = new EncodeRequest(url);
        
        // When
        Set<ConstraintViolation<EncodeRequest>> violations = validator.validate(request);
        
        // Then
        assertTrue(violations.isEmpty(), "URL with exactly 2048 characters should be valid");
        assertEquals(url, request.getUrl());
    }
}