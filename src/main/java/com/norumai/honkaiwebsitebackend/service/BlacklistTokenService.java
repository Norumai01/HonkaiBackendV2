package com.norumai.honkaiwebsitebackend.service;

import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class BlacklistTokenService {

    private final RedisCommands<String, Object> redisCommands;
    private final static Logger logger = LoggerFactory.getLogger(BlacklistTokenService.class);

    @Autowired
    public BlacklistTokenService(RedisCommands<String, Object> redisCommands) {
        this.redisCommands = redisCommands;
    }

    public void blacklistToken(String token, String email) {
        String hashedToken = hashToken(token);
        String keyName = "jwt-blacklist:" + hashedToken;
        logger.debug("Blacklisting token for the user, {}...", email);
        redisCommands.setex(keyName, 7200, email); // Expires in 2 hours.
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, hash).toString(16);
        }
        catch (Exception e) {
            throw new RuntimeException("Error while hashing token.", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String hashedToken = hashToken(token);
        String keyName = "jwt-blacklist:" + hashedToken;
        return redisCommands.exists(keyName) == 1;
    }
}
