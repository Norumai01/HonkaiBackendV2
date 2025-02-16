package com.norumai.honkaiwebsitebackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "Email", nullable = false, unique = true, length = 320)
    private String email;

    // Ignore password in JSON output.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "Password", nullable = false, length = 75)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Create on new account.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
