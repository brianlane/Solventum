package com.solventum.shortlink.controller;

import com.solventum.shortlink.model.DecodeRequest;
import com.solventum.shortlink.model.DecodeResponse;
import com.solventum.shortlink.model.EncodeRequest;
import com.solventum.shortlink.model.EncodeResponse;
import com.solventum.shortlink.service.UrlShorteningService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
    private final UrlShorteningService urlShorteningService;
    
    @Autowired
    public ShortlinkController(UrlShorteningService urlShorteningService) {
        this.urlShorteningService = urlShorteningService;
    }
    
    /**
     * Encodes a long URL into a short URL.
     * 
     * @param request the encode request containing the long URL
     * @return ResponseEntity containing the short URL and original URL
     */
    @PostMapping("/encode")
    public ResponseEntity<EncodeResponse> encodeUrl(@Valid @RequestBody EncodeRequest request) {
        try {
            String shortUrl = urlShorteningService.encodeUrl(request.getUrl());
            EncodeResponse response = new EncodeResponse(shortUrl, request.getUrl());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Return 400 Bad Request for invalid URLs
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Return 500 Internal Server Error for unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Decodes a short URL back to its original long URL.
     * 
     * @param request the decode request containing the short URL
     * @return ResponseEntity containing the original URL and short URL
     */
    @PostMapping("/decode")
    public ResponseEntity<DecodeResponse> decodeUrl(@Valid @RequestBody DecodeRequest request) {
        try {
            String originalUrl = urlShorteningService.decodeUrl(request.getShortUrl());
            DecodeResponse response = new DecodeResponse(originalUrl, request.getShortUrl());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Return 404 Not Found for URLs that don't exist or invalid format
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Return 500 Internal Server Error for unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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