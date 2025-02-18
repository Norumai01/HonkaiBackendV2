package com.norumai.honkaiwebsitebackend.controller;

import com.norumai.honkaiwebsitebackend.service.UserService;
import com.norumai.honkaiwebsitebackend.model.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

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
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            if (userService.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("User with this email already exists.");
            }
            if (userService.findByUsername(user.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("User with this username already exists.");
            }

            User savedUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Error occurred while creating user.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody User userRequest) {
        try {
            attemptToAuthenticate(userRequest.getEmail(), userRequest.getUsername(), userRequest.getPassword());

            // Intended user data is obtained and not the input credential with missing data.
            User user = userService.findByEmail(userRequest.getEmail())
                    .or(() -> userService.findByUsername(userRequest.getUsername()))
                    .orElseThrow(() -> new UsernameNotFoundException("Username or Email not found."));

            return ResponseEntity.ok().body(user);
        }
        catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials.");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Error occurred while attempting to login.");
        }
    }
    private void attemptToAuthenticate(String email, String username, String password) {
        if (email != null && userService.findByEmail(email).isPresent()) {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            return;
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

}
