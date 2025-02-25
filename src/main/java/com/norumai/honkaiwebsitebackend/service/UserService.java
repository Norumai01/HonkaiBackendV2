package com.norumai.honkaiwebsitebackend.service;

import com.norumai.honkaiwebsitebackend.dto.RegisterRequest;
import com.norumai.honkaiwebsitebackend.model.User;
import com.norumai.honkaiwebsitebackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user with username: {}...", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user with email: {}...", email);
        return userRepository.findByEmail(email);
    }

    public User createUser(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        if (registerRequest.getBio() != null) {
            user.setBio(registerRequest.getBio());
        }

        logger.info("User created successfully for: {}.", user.getUsername());
        return userRepository.save(user);
    }

    // Update user implementation here

    // Delete user implementation here


}
