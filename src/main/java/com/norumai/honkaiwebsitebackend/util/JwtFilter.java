package com.norumai.honkaiwebsitebackend.util;

import com.norumai.honkaiwebsitebackend.service.CustomUserDetailsService;
import com.norumai.honkaiwebsitebackend.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter { // OncePerRequestFilter verifies once, good for token bearer like JWT.

    private final CustomUserDetailsService userDetailsService;
    private final JWTService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    public JwtFilter(CustomUserDetailsService userDetailsService, JWTService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        logger.debug("Processed request authentication header: {}.", authHeader != null ? "Authorization" : "Null");

        String token = null;
        String email = null;

        // Validate from "Bearer {token}"
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.debug("Token received has been received.");

            // Valid JWT token is "{header}.{Payload}.{Signature}".
            if (!token.contains(".") || token.split("\\.").length != 3) {
                logger.error("Invalid JWT format detected: {}.", token);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT format");
                return;
            }

            try {
                email = jwtService.extractEmail(token);
                logger.debug("Token's Email found: {}.", email);
            }
            catch (Exception e) {
                logger.error("Invalid JWT Token detected: {}.", token);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT Token");
                return;
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // UserDetails has been custom set to use User's Email for authentication.
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtService.validateToken(token, userDetails)) {
                logger.info("Token successfully validated for: {}.", userDetails.getUsername());
                UsernamePasswordAuthenticationToken userPassAuthToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                userPassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(userPassAuthToken);
            }
            else {
                logger.error("Token validation failed for user: {}.", userDetails.getUsername());
            }
        }

        filterChain.doFilter(request, response);
    }
}
