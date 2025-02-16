package com.norumai.honkaiwebsitebackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HonkaiWebsiteBackendApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure().load();

        // Set environment variables
        System.setProperty("MYSQL_USER", dotenv.get("MYSQL_USER"));
        System.setProperty("MYSQL_PASSWORD", dotenv.get("MYSQL_PASSWORD"));

        SpringApplication.run(HonkaiWebsiteBackendApplication.class, args);
    }
}
