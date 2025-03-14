package com.norumai.honkaiwebsitebackend.config;

import com.norumai.honkaiwebsitebackend.util.Jackson2JsonRedisCodec;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LettuceRedisConfig {
    private static final Logger logger = LoggerFactory.getLogger(LettuceRedisConfig.class);

    private final Jackson2JsonRedisCodec<String, Object> jackson2JsonRedisCodec;
    @Autowired
    public LettuceRedisConfig(Jackson2JsonRedisCodec<String, Object> jackson2JsonRedisCodec) {
        this.jackson2JsonRedisCodec = jackson2JsonRedisCodec;
    }

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.username:}")    // Use :{some value} to set a default value.
    private String username;

    @Value("${redis.password:}")
    private String password;

    @Value("${redis.database:0}")
    private int database;

    @Value("${redis.timeout:3000}")
    private int timeout;

    @Bean
    public RedisURI redisURI() {
        logger.info("Initializing Redis URI with host: {} and port: {}...", host, port);
        RedisURI.Builder builder = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withDatabase(database)
                .withAuthentication(username, password)
                .withTimeout(Duration.ofMillis(timeout));

        return builder.build();
    }

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient(RedisURI redisURI) {
        RedisClient redisClient = RedisClient.create(redisURI);
        redisClient.setOptions(ClientOptions.builder()
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .build());
        logger.debug("Initializing Redis Client: {}", redisClient);
        return redisClient;
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, Object> redisConnection(RedisClient redisClient) {
        logger.debug("Initializing connection to Redis database: {}", redisClient);
        return redisClient.connect(jackson2JsonRedisCodec);
    }

    @Bean
    public RedisCommands<String, Object> redisCommands(StatefulRedisConnection<String, Object> redisConnection) {
        logger.info("Commands can be now executed to Redis database.");
        return redisConnection.sync();
    }
}
