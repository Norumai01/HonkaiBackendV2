package com.norumai.honkaiwebsitebackend.controller;

import com.norumai.honkaiwebsitebackend.dto.LoginRequest;
import com.norumai.honkaiwebsitebackend.dto.RegisterRequest;
import com.norumai.honkaiwebsitebackend.service.JWTService;
import com.norumai.honkaiwebsitebackend.service.UserService;
import com.norumai.honkaiwebsitebackend.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(UserService userService, JWTService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            logger.info("All users found.");
            return ResponseEntity.status(HttpStatus.OK).body(users);
        }
        catch (Exception e) {
            logger.error("Error getting all users.", e);
            return ResponseEntity.badRequest().body("Unable to obtain the list of all users.");
        }
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
                logger.warn("User with email: {} already exists.", registerRequest.getEmail());
                return ResponseEntity.badRequest().body("User with this email already exists.");
            }
            if (userService.findByUsername(registerRequest.getUsername()).isPresent()) {
                logger.warn("User with username: {} already exists.", registerRequest.getUsername());
                return ResponseEntity.badRequest().body("User with this username already exists.");
            }

            User savedUser = userService.createUser(registerRequest);
            logger.info("Created user for {}.", savedUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        }
        catch (Exception e) {
            logger.error("Error creating user.", e);
            return ResponseEntity.badRequest().body("Error occurred while creating user.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            attemptToAuthenticate(loginRequest.getUserInput(), loginRequest.getPassword());

            // Intended user data is obtained and not the input credential with missing data.
            User user = userService.findByEmail(loginRequest.getUserInput())
                    .or(() -> userService.findByUsername(loginRequest.getUserInput()))
                    .orElseThrow(() -> {
                        logger.warn("User with email: {} not found.", loginRequest.getUserInput());
                        return new UsernameNotFoundException("Username or Email not found.");
                    });

            // User's Token (Identity) for accessing API Requests.
            String jwtKey = jwtService.generateToken(user);

            Map<String, Object> responses = new HashMap<>();
            responses.put("token", jwtKey);
            responses.put("user", user);

            logger.info("Successfully logged in: {}.", user.getUsername());
            return ResponseEntity.ok().body(responses);
        }
        catch (AuthenticationException e) {
            logger.error("Invalid credentials provided for: {}.", loginRequest.getUserInput());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials.");
        }
        catch (Exception e) {
            logger.error("Error logging in for user: " + loginRequest.getUserInput(), e);
            return ResponseEntity.badRequest().body("Error occurred while attempting to login.");
        }
    }
    private void attemptToAuthenticate(String userInput, String password) {
        logger.warn("Attempting to authenticate: {}...", userInput);
        if (userInput != null && userService.findByEmail(userInput).isPresent()) {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userInput, password));
            return;
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userInput, password));
    }

}
