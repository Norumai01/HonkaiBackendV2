package com.norumai.honkaiwebsitebackend.controller;

import com.norumai.honkaiwebsitebackend.dto.LoginRequest;
import com.norumai.honkaiwebsitebackend.dto.RegisterRequest;
import com.norumai.honkaiwebsitebackend.service.JWTService;
import com.norumai.honkaiwebsitebackend.service.UserService;
import com.norumai.honkaiwebsitebackend.model.User;
import jakarta.validation.Valid;
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

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.status(HttpStatus.OK).body(users);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Unable to obtain the list of all users.");
        }
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("User with this email already exists.");
            }
            if (userService.findByUsername(registerRequest.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("User with this username already exists.");
            }

            User savedUser = userService.createUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        }
        catch (Exception e) {
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
                    .orElseThrow(() -> new UsernameNotFoundException("Username or Email not found."));

            String jwtKey = jwtService.generateToken(user);

            Map<String, Object> responses = new HashMap<>();
            responses.put("token", jwtKey);
            responses.put("user", user);

            return ResponseEntity.ok().body(responses);
        }
        catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials.");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Error occurred while attempting to login.");
        }
    }
    private void attemptToAuthenticate(String userInput, String password) {
        if (userInput != null && userService.findByEmail(userInput).isPresent()) {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userInput, password));
            return;
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userInput, password));
    }

}
