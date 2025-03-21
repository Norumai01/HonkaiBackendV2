package com.norumai.honkaiwebsitebackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableCaching
public class HonkaiWebsiteBackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(HonkaiWebsiteBackendApplication.class);

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure().load();

        // Set environment variables
        System.setProperty("MYSQL_USER", dotenv.get("MYSQL_USER"));
        System.setProperty("MYSQL_PASSWORD", dotenv.get("MYSQL_PASSWORD"));
        System.setProperty("CORS_ALLOWED_ORIGIN", dotenv.get("CORS_ALLOWED_ORIGIN"));

        // Redis
        System.setProperty("REDIS_HOST", dotenv.get("REDIS_HOST"));
        System.setProperty("REDIS_PORT", dotenv.get("REDIS_PORT"));
        System.setProperty("REDIS_USER", dotenv.get("REDIS_USER"));
        System.setProperty("REDIS_PASSWORD", dotenv.get("REDIS_PASSWORD"));

        // SSL/TLS Certificate and HTTPS
        System.setProperty("SSL_KEYSTORE_PATH", dotenv.get("SSL_KEYSTORE_PATH"));
        System.setProperty("SSL_KEYSTORE_PASSWORD", dotenv.get("SSL_KEYSTORE_PASSWORD"));

        logger.info("Application starting...");
        SpringApplication.run(HonkaiWebsiteBackendApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application is ready to accept API requests.");
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown() {
        logger.info("Application is shutting down...");
    }
}
