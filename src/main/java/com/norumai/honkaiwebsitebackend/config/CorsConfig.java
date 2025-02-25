package com.norumai.honkaiwebsitebackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    // https://www.geeksforgeeks.org/spring-security-cors-configuration/
    // https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html

    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Bean
    public CorsConfigurationSource corsConfiguration() {
        String allowedOrigin = System.getenv("CORS_ALLOWED_ORIGIN");

        logger.info("Configuring CORS setting...");
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin(allowedOrigin);
        config.setAllowedHeaders(List.of(
                "Content-Type",          // For sending JSON/form data
                "Authorization",
                "Accept",
                "Origin",               // Required for CORS
                "X-XSRF-TOKEN"          // Allows CSRF protection. (Future implementation)
        ));
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"   // For CORS Preflight requests.
        ));

        // Allows CSRF protection. (Future implementation)
        config.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        logger.info("CORS configured to allow origin at {}", allowedOrigin);
        return source;
    }
}
