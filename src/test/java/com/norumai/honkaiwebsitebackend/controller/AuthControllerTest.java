package com.norumai.honkaiwebsitebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.norumai.honkaiwebsitebackend.dto.LoginRequest;
import com.norumai.honkaiwebsitebackend.model.User;
import com.norumai.honkaiwebsitebackend.service.BlacklistTokenService;
import com.norumai.honkaiwebsitebackend.service.CustomUserDetailsService;
import com.norumai.honkaiwebsitebackend.service.JWTService;
import com.norumai.honkaiwebsitebackend.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use the test properties file for testing.
public class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    public AuthControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Mock
    private AuthController authController;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private BlacklistTokenService blacklistTokenService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final static Logger logger = LoggerFactory.getLogger(AuthControllerTest.class);

    @Test
    void login_WithValidCredentials() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserInput("test@example.com"); // Username or email can be used.
        loginRequest.setPassword("password123");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock.jwt.token");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().value("jwt", "mock.jwt.token"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andReturn();

        Cookie jwtCookie = result.getResponse().getCookie("jwt");
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isEqualTo("mock.jwt.token");
        assertThat(jwtCookie.getMaxAge()).isEqualTo(7200);
        assertThat(jwtCookie.isHttpOnly()).isTrue();
        assertThat(jwtCookie.getSecure()).isTrue();

        logger.info("Tested login method was successful.");
    }

    @Test
    void logout_BlacklistTokenAndClearCookie() throws Exception {
        Cookie jwtCookie = new Cookie("jwt", "mock.jwt.token");
        String email = "test@example.com";
        when(jwtService.extractEmail("mock.jwt.token")).thenReturn(email);

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email,
                "password123",
                authorities
        );

        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.validateToken("mock.jwt.token", userDetails)).thenReturn(true);

        MvcResult result = mockMvc.perform(post("/auth/logout")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("jwt", 0))
                .andReturn();

        verify(blacklistTokenService).blacklistToken("mock.jwt.token", "test@example.com");
        logger.info("Tested logout method was successful.");
    }
}
