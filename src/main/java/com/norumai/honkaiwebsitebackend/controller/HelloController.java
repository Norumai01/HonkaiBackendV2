package com.norumai.honkaiwebsitebackend.controller;

import com.norumai.honkaiwebsitebackend.model.User;
import com.norumai.honkaiwebsitebackend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final UserRepository userRepository;

    @Autowired
    public HelloController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Testing orElseThrow()
    @GetMapping("/test/{email}")
    public String test(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return "Found user: " + user.getUsername();
    }

    @GetMapping("/")
    public String hello(HttpServletRequest request) {
        try {
            return "Hello World" + request.getSession().getId();
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }
}
