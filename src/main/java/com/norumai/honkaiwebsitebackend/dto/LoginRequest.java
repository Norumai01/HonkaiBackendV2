package com.norumai.honkaiwebsitebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username or Email must be provided.")
    private String userInput;

    @NotBlank(message = "Password must be provided.")
    private String password;
}
