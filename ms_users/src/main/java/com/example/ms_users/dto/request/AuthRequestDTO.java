package com.example.ms_users.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Solicitud de autenticación (login)")
public class AuthRequestDTO {
    @NotBlank(message = "Username requerido")
    @Schema(description = "Nombre de usuario",
            example = "Fernando Carnaca Supremo I",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password requerido")
    @Schema(description = "Contraseña",
            example = "Mimamamemima321",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}