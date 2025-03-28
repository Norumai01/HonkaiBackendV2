package com.norumai.honkaiwebsitebackend.util;

import com.norumai.honkaiwebsitebackend.service.BlacklistTokenService;
import com.norumai.honkaiwebsitebackend.service.CustomUserDetailsService;
import com.norumai.honkaiwebsitebackend.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
    private final BlacklistTokenService blacklistTokenService;
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    public JwtFilter(CustomUserDetailsService userDetailsService, JWTService jwtService, BlacklistTokenService blacklistTokenService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.blacklistTokenService = blacklistTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        logger.debug("Processed request authentication header: {}.", authHeader != null ? "Authorization" : "Null");

        String token = null;
        String email = null;
        Cookie[] cookies = request.getCookies();

        // User whose are logged in, have cookies.
        // TODO: Potential security flaw here, attacker striking if somehow obtain JWT token from cookie without being logged in.
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("jwt")) {
                    token = cookie.getValue();
                    logger.debug("Token has been received from cookie.");
                    break;
                }
            }
        }
        else {
            // If no account found, no need to authenticate because no account found.
            logger.debug("No account or cookie found.");
        }

        if (token != null) {
            // Valid JWT token is "{header}.{Payload}.{Signature}".
            if (!token.contains(".") || token.split("\\.").length != 3) {
                logger.error("Invalid JWT format detected.");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT format");
                return;
            }

            // Check if the current token is blacklisted.
            if (blacklistTokenService.isTokenBlacklisted(token)) {
                logger.warn("Blacklisted token detected.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Blacklisted token detected.");
                return;
            }

            try {
                email = jwtService.extractEmail(token);
                logger.debug("Token of the email has been found.");
            }
            catch (Exception e) {
                logger.error("JWT Token cannot be identified.");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT Token");
                return;
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // UserDetails has been custom set to use User's Email for authentication.
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtService.validateToken(token, userDetails)) {
                logger.info("Token successfully validated for the email.");
                UsernamePasswordAuthenticationToken userPassAuthToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                userPassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(userPassAuthToken);
            }
            else {
                logger.error("Token validation failed.");
            }
        }

        filterChain.doFilter(request, response);
    }
}
