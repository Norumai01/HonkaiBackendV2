package com.norumai.honkaiwebsitebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @Size(min = 2, max = 320, message = "Username or Email is too long or short.")
    @NotBlank(message = "Username or Email must be provided.")
    private String userInput;

    @Size(min = 5, max = 75, message = "Password is too long or short.")
    @NotBlank(message = "Password must be provided.")
    private String password;
}
