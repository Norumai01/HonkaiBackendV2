package com.norumai.honkaiwebsitebackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Size(min = 2, max = 50, message = "Username is too long or short.")
    @NotBlank(message = "Username must be provided.")
    private String username;

    @Size(min = 2, max = 320, message = "Email is too long or short.")
    @Email(message = "Email should be valid.")
    @NotBlank(message = "Email must be provided.")
    private String email;

    @Size(min = 5, max = 75, message = "Password is too long or short.")
    @NotBlank(message = "Password must be provided.")
    private String password;

    private String bio;

}
