package com.solventum.shortlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the ShortLink URL shortening service.
 * 
 * This application provides REST endpoints to encode long URLs into short URLs
 * and decode short URLs back to their original long URLs.
 */
@SpringBootApplication
public class ShortlinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortlinkApplication.class, args);
    }
}