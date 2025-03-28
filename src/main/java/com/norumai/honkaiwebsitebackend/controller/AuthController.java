package com.norumai.honkaiwebsitebackend.controller;

import com.norumai.honkaiwebsitebackend.dto.LoginRequest;
import com.norumai.honkaiwebsitebackend.dto.RegisterRequest;
import com.norumai.honkaiwebsitebackend.service.BlacklistTokenService;
import com.norumai.honkaiwebsitebackend.service.JWTService;
import com.norumai.honkaiwebsitebackend.service.UserService;
import com.norumai.honkaiwebsitebackend.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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
    private final BlacklistTokenService blacklistTokenService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final String GENERIC_AUTH_ERROR = "Authentication failed. Please try again later.";
    private static final String GENERIC_REGISTRATION_ERROR = "Registration could not be completed. Please try again later.";

    @Autowired
    public AuthController(UserService userService, JWTService jwtService, AuthenticationManager authenticationManager, BlacklistTokenService blacklistTokenService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.blacklistTokenService = blacklistTokenService;
    }

    // May delete this later, feel unnecessary.
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

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(@Valid
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "bio", required = false) String bio) {
        try {
            // Validate Inputs
            if (username == null || username.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                logger.warn("Missing required registration fields");
                return ResponseEntity.badRequest().body("All required fields must be provided");
            }

            // Identify matching username or email.
            if (userService.findByUsername(username).isPresent() || userService.findByEmail(email).isPresent()) {
                logger.warn("Registration attempt with existing credentials");
                return ResponseEntity.badRequest().body("Registration failed. Please try with different credentials.");
            }

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername(username);
            registerRequest.setEmail(email);
            registerRequest.setPassword(password);
            registerRequest.setBio(bio);

            User savedUser = userService.createUser(registerRequest);
            logger.info("Created a new user.");
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        }
        catch (Exception e) {
            logger.error("User registration error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GENERIC_REGISTRATION_ERROR);
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
                        logger.warn("User with inputted email is not found.");
                        return new UsernameNotFoundException("Credentials not found.");
                    });

            // User's Token (Identity) for accessing API Requests.
            String jwtKey = jwtService.generateToken(user);

            // Securing User's token behind cookie.
            ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwtKey)
                    .httpOnly(true) // Prevent XSS attacks.
                    .secure(true) // Protect from being intercepted.
                    .path("/")
                    .maxAge(7200) // 2 hours
                    .sameSite("None")
                    .build();

            Map<String, Object> responses = new HashMap<>();
            responses.put("user", user);

            logger.info("Successfully logged in.");
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(responses);
        }
        catch (AuthenticationException e) {
            logger.error("Authentication failed: Invalid credentials or account not found.\n", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or account not found.");
        }
        catch (Exception e) {
            logger.error("Authentication failed: ", e);
            return ResponseEntity.badRequest().body(GENERIC_AUTH_ERROR);
        }
    }
    private void attemptToAuthenticate(String userInput, String password) {
        logger.debug("Authentication attempt received");
        if (userInput != null && userService.findByEmail(userInput).isPresent()) {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userInput, password));
            return;
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userInput, password));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            ResponseCookie clearCookie = ResponseCookie.from("jwt","")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0) // Clear the cookie.
                    .sameSite("None")
                    .build();

            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("jwt")) {
                    String token = cookie.getValue();
                    String email = jwtService.extractEmail(token);

                    // Add token to Redis service to be blacklisted.
                    blacklistTokenService.blacklistToken(token, email);
                    logger.info("Successfully blacklisted the current token for the user.");
                    break;
                }
            }

            logger.info("Successfully logged out.");
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                    .body("Logged out successfully.");
        }
        catch (Exception e) {
            logger.error("Logout error", e);
            // For ensuring secured details, return 200 regardless whether user is logged out or not.
            return ResponseEntity.ok().body("Logged out successfully.");
        }
    }

}
