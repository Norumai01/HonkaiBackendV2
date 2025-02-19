package com.norumai.honkaiwebsitebackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username must be provided.")
    private String username;

    @Email(message = "Email should be valid.")
    @NotBlank(message = "Email must be provided.")
    private String email;

    @NotBlank(message = "Password must be provided.")
    private String password;

    private String bio;

}
