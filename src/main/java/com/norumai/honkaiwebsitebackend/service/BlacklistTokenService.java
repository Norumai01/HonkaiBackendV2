package com.norumai.honkaiwebsitebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlacklistTokenService {

    private final RedisCommands<String, String> redisCommands;
    private final static Logger logger = LoggerFactory.getLogger(BlacklistTokenService.class);

    @Autowired
    public BlacklistTokenService(RedisCommands<String, String> redisCommands) {
        this.redisCommands = redisCommands;
    }
}
