package com.norumai.honkaiwebsitebackend.config;

import com.norumai.honkaiwebsitebackend.util.Jackson2JsonRedisCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisCodecConfig {
    private final static Logger logger = LoggerFactory.getLogger(RedisCodecConfig.class);

    @Bean
    public Jackson2JsonRedisCodec<String, Object> objectRedisCodec() {
        logger.info("Any Object classes can be serialized/deserialized using Redis.");
        return new Jackson2JsonRedisCodec<>(String.class, Object.class);
    }
}
