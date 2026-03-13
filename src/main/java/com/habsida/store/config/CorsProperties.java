package com.habsida.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        Long maxAgeSeconds,
        Boolean allowCredentials
) {
    public CorsProperties {
        allowedOrigins = allowedOrigins != null ? allowedOrigins : List.of("http://localhost:3000", "http://localhost:8080");
        allowedMethods = allowedMethods != null ? allowedMethods : List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        allowedHeaders = allowedHeaders != null ? allowedHeaders : List.of("Authorization", "Content-Type", "Accept", "X-Requested-With");
        exposedHeaders = exposedHeaders != null ? exposedHeaders : List.of();
        maxAgeSeconds = maxAgeSeconds != null ? maxAgeSeconds : 3600L;
        allowCredentials = allowCredentials != null ? allowCredentials : true;
    }
}
