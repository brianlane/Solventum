package com.solventum.shortlink.controller;

import com.solventum.shortlink.model.DecodeRequest;
import com.solventum.shortlink.model.DecodeResponse;
import com.solventum.shortlink.model.EncodeRequest;
import com.solventum.shortlink.model.EncodeResponse;
import com.solventum.shortlink.service.UrlShorteningService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Semaphore;

/**
 * REST Controller for URL shortening operations.
 * 
 * Provides endpoints for encoding long URLs to short URLs and decoding
 * short URLs back to their original form.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow CORS for testing
public class ShortlinkController {
    
    private static final Logger logger = LoggerFactory.getLogger(ShortlinkController.class);
    
    private final UrlShorteningService urlShorteningService;
    private final Semaphore requestSemaphore;
    
    @Autowired
    public ShortlinkController(UrlShorteningService urlShorteningService, Semaphore requestSemaphore) {
        this.urlShorteningService = urlShorteningService;
        this.requestSemaphore = requestSemaphore;
    }
    
    /**
     * Encodes a long URL into a short URL.
     * Respects concurrency limits - returns 429 if too many concurrent requests.
     * 
     * @param request the encode request containing the long URL
     * @return ResponseEntity containing the short URL and original URL
     */
    @PostMapping("/encode")
    public ResponseEntity<EncodeResponse> encodeUrl(@Valid @RequestBody EncodeRequest request) {
        return executeWithConcurrencyControl(() -> {
            logger.info("Received encode request for URL: {}", request.getUrl());
            String shortUrl = urlShorteningService.encodeUrl(request.getUrl());
            EncodeResponse response = new EncodeResponse(shortUrl, request.getUrl());
            logger.debug("Encode successful, returning response: {}", response);
            return ResponseEntity.ok(response);
        });
    }
    
    /**
     * Decodes a short URL back to its original long URL.
     * Respects concurrency limits - returns 429 if too many concurrent requests.
     * 
     * @param request the decode request containing the short URL
     * @return ResponseEntity containing the original URL and short URL
     */
    @PostMapping("/decode")
    public ResponseEntity<DecodeResponse> decodeUrl(@Valid @RequestBody DecodeRequest request) {
        return executeWithConcurrencyControl(() -> {
            logger.info("Received decode request for short URL: {}", request.getShortUrl());
            String originalUrl = urlShorteningService.decodeUrl(request.getShortUrl());
            DecodeResponse response = new DecodeResponse(originalUrl, request.getShortUrl());
            logger.debug("Decode successful, returning response: {}", response);
            return ResponseEntity.ok(response);
        });
    }
    
    /**
     * Health check endpoint to verify the service is running.
     * 
     * @return simple status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ShortLink service is running");
    }
    
    /**
     * Get statistics about the service.
     * Useful for monitoring and debugging.
     * 
     * @return service statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ServiceStats> getStats() {
        ServiceStats stats = new ServiceStats(urlShorteningService.getUrlMappingSize());
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Executes a function with concurrency control.
     * If no permit is available, returns HTTP 429 (Too Many Requests).
     * 
     * @param function the function to execute
     * @return ResponseEntity with result or 429 status
     */
    private <T> ResponseEntity<T> executeWithConcurrencyControl(java.util.function.Supplier<ResponseEntity<T>> function) {
        if (requestSemaphore.tryAcquire()) {
            try {
                logger.debug("Acquired permit, processing request. Available permits: {}", requestSemaphore.availablePermits());
                return function.get();
            } catch (IllegalArgumentException e) {
                logger.warn("Request failed with validation error: {}", e.getMessage());
                if (e.getMessage().contains("not found") || e.getMessage().contains("Invalid short URL")) {
                    return ResponseEntity.notFound().build();
                } else {
                    return ResponseEntity.badRequest().build();
                }
            } catch (Exception e) {
                logger.error("Unexpected error during request processing: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            } finally {
                requestSemaphore.release();
                logger.debug("Released permit. Available permits: {}", requestSemaphore.availablePermits());
            }
        } else {
            logger.warn("Request rejected - service is busy. Available permits: {}", requestSemaphore.availablePermits());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
    
    /**
     * Simple statistics holder class.
     */
    public static class ServiceStats {
        private final int totalUrls;
        private final long timestamp;
        
        public ServiceStats(int totalUrls) {
            this.totalUrls = totalUrls;
            this.timestamp = System.currentTimeMillis();
        }
        
        public int getTotalUrls() {
            return totalUrls;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}