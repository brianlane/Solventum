package com.solventum.shortlink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

/**
 * Configuration for concurrency control.
 * 
 * Provides a configurable Semaphore to limit the number of concurrent requests
 * that can be processed by the URL shortening endpoints.
 */
@Configuration
public class ConcurrencyConfig {
    
    @Value("${app.max.concurrent.requests:10}")
    private int maxConcurrentRequests;
    
    /**
     * Creates a Semaphore with configurable permits to control concurrent access.
     * 
     * @return Semaphore with the configured number of permits
     */
    @Bean
    public Semaphore requestSemaphore() {
        return new Semaphore(maxConcurrentRequests);
    }
}