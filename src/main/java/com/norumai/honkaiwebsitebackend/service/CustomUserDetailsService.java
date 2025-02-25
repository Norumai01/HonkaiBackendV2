package com.norumai.honkaiwebsitebackend.service;

import com.norumai.honkaiwebsitebackend.repository.UserRepository;
import com.norumai.honkaiwebsitebackend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // UserDetails is overrided to use User's email information.
    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {
        logger.debug("Attempting to load user details for {}...", loginInput);

        try {
            User user = userRepository.findByEmail(loginInput)
                    .or(() -> userRepository.findByUsername(loginInput))
                    .orElseThrow(() -> {
                        logger.warn("Failed login attempt - user not found: {}.", loginInput);
                        return new UsernameNotFoundException("User not found with email or username: " + loginInput);
                    });

            logger.info("User details loaded successfully for: {}.", loginInput);
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
        }
        catch (Exception e) {
            logger.error("Unexpected error loading user: {}.", loginInput, e);
            throw new UsernameNotFoundException("Error loading user: " + loginInput);
        }
    }
}
