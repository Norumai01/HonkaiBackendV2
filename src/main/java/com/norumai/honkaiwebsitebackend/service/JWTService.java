package com.norumai.honkaiwebsitebackend.service;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;
import com.norumai.honkaiwebsitebackend.model.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {

    // Build secret key signature with HS256 hashing algorithm.
    private final SecretKey key = Jwts.SIG.HS256.key().build();

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getUsername());
    }

    private String createToken(Map<String, Object> claims, String userSubject) {
        return Jwts.builder()
                .claims(claims)
                .subject(userSubject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2)) // 2 hours
                .signWith(key)
                .compact();
    }

}
