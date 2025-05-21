package com.solventum.shortlink.controller;

import com.solventum.shortlink.model.EncodeRequest;
import com.solventum.shortlink.model.DecodeRequest;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for concurrency control functionality.
 * 
 * Tests that the service properly limits concurrent requests and returns
 * HTTP 429 (Too Many Requests) when the limit is exceeded.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, 
                properties = {"app.max.concurrent.requests=3"})
class ConcurrencyControlTest {
    
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
    void testConcurrencyLimitEnforce() throws InterruptedException {
        // Given - More threads than allowed concurrent requests
        int numThreads = 10;
        int maxConcurrent = 3; // Set in @SpringBootTest properties
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        
        // When - Launch multiple concurrent requests
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();
                    
                    // Make request
                    EncodeRequest request = new EncodeRequest("https://example" + threadId + ".com");
                    HttpEntity<EncodeRequest> entity = new HttpEntity<>(request, headers);
                    
                    ResponseEntity<String> response = restTemplate.exchange(
                        baseUrl + "/encode", HttpMethod.POST, entity, String.class);
                    
                    // Count response types
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        rejectedCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                    
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            }).start();
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS), "All threads should complete within 10 seconds");
        
        // Then - Verify concurrency control worked
        assertEquals(numThreads, successCount.get() + rejectedCount.get() + errorCount.get(), 
            "All requests should be accounted for");
        
        assertTrue(rejectedCount.get() > 0, 
            "Some requests should be rejected due to concurrency limit");
        
        assertTrue(successCount.get() > 0, 
            "Some requests should succeed");
        
        assertTrue(successCount.get() <= maxConcurrent * 2, 
            "Success count should be reasonable given concurrency limit");
        
        assertEquals(0, errorCount.get(), 
            "Should not have any unexpected errors");
        
        System.out.printf("Results: %d successful, %d rejected, %d errors%n", 
            successCount.get(), rejectedCount.get(), errorCount.get());
    }
    
    @Test
    void testConcurrencyLimitForDecode() throws InterruptedException {
        // Given - Pre-populate with some URLs
        String shortUrl1 = urlShorteningService.encodeUrl("https://test1.com");
        String shortUrl2 = urlShorteningService.encodeUrl("https://test2.com");
        String shortUrl3 = urlShorteningService.encodeUrl("https://test3.com");
        
        int numThreads = 8;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();
        
        String[] testUrls = {shortUrl1, shortUrl2, shortUrl3};
        
        // When - Launch multiple concurrent decode requests
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    
                    // Use different short URLs cyclically
                    String shortUrl = testUrls[threadId % testUrls.length];
                    DecodeRequest request = new DecodeRequest(shortUrl);
                    HttpEntity<DecodeRequest> entity = new HttpEntity<>(request, headers);
                    
                    ResponseEntity<String> response = restTemplate.exchange(
                        baseUrl + "/decode", HttpMethod.POST, entity, String.class);
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        rejectedCount.incrementAndGet();
                    }
                    
                } catch (Exception e) {
                    // Ignore exceptions for this test
                } finally {
                    finishLatch.countDown();
                }
            }).start();
        }
        
        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        
        // Then - Verify decode also respects concurrency limits
        assertTrue(rejectedCount.get() > 0, 
            "Decode should also reject requests when limit exceeded");
        
        assertTrue(successCount.get() > 0, 
            "Some decode requests should succeed");
        
        System.out.printf("Decode results: %d successful, %d rejected%n", 
            successCount.get(), rejectedCount.get());
    }
    
    @Test
    void testSequentialRequestsAllSucceed() {
        // Given - Sequential requests (no concurrency)
        String[] testUrls = {
            "https://sequential1.com",
            "https://sequential2.com", 
            "https://sequential3.com",
            "https://sequential4.com",
            "https://sequential5.com"
        };
        
        // When - Make requests one after another
        for (String url : testUrls) {
            EncodeRequest request = new EncodeRequest(url);
            HttpEntity<EncodeRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/encode", HttpMethod.POST, entity, String.class);
            
            // Then - All should succeed since no concurrency
            assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "Sequential request should succeed for: " + url);
        }
    }
    
    @Test
    void testHealthAndStatsNotAffectedByConcurrencyLimit() throws InterruptedException {
        // Given - Health and stats endpoints should not be subject to concurrency control
        int numThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger();
        
        // When - Multiple concurrent requests to health endpoint
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    
                    ResponseEntity<String> response = restTemplate.getForEntity(
                        baseUrl + "/health", String.class);
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    }
                    
                } catch (Exception e) {
                    // Ignore exceptions for this test
                } finally {
                    finishLatch.countDown();
                }
            }).start();
        }
        
        startLatch.countDown();
        assertTrue(finishLatch.await(5, TimeUnit.SECONDS));
        
        // Then - All health checks should succeed
        assertEquals(numThreads, successCount.get(), 
            "All health checks should succeed regardless of concurrency limit");
    }
}