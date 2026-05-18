package com.example.ms_users.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDTO {
    @NotBlank(message = "Username requerido")
    private String username;

    @NotBlank(message = "Password requerido")
    private String password;
}