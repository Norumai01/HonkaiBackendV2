package com.norumai.honkaiwebsitebackend.config;

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

    @Bean
    public CorsConfigurationSource corsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
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
        return source;
    }
}
