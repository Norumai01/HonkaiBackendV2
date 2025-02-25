package com.norumai.honkaiwebsitebackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.norumai.honkaiwebsitebackend.model.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);
    // Build secret key signature with HS256 hashing algorithm.
    private final SecretKey key = Jwts.SIG.HS256.key().build();

    @PostConstruct
    public void init() {
        logger.info("JWT service initializing...");
    }

    public String generateToken(User user) {
        logger.debug("Generating token for: {}.", user.getUsername());
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getEmail());
    }

    private String createToken(Map<String, Object> claims, String userSubject) {
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2);
        logger.debug("Creating token for {} with expiration date of {}.", userSubject, expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(userSubject)
                .issuedAt(issuedAt)
                .expiration(expiration) // 2 hours
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        // CustomUserDetailsService is set to use Email in UserDetails' username.
        // Token is validated from their email.
        try {
            final String email = extractEmail(token);


            if (!email.equals(userDetails.getUsername())) {
                logger.warn("Token's and User's email do not match.");
                return false;
            }

            if (isTokenExpired(token)) {
                logger.warn("Token expired for: {}.", userDetails.getUsername());
                return false;
            }

            logger.info("Token successfully validated for: {}.", userDetails.getUsername());
            return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
        }
        catch (Exception e) {
            logger.error("Error validating token: {}.", token, e);
            return false;
        }
    }
}
