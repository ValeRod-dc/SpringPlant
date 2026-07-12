package com.example.ms_users.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Solicitud de registro de nuevo usuario")
public class UserRequestDTO {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    @Schema(description = "Nombre de usuario", example ="Fernando Carnaca Supremo I", minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Schema(description = "Correo electrónico", example = "fer_carnaca@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Schema(description = "Contraseña (mínima 6 caracteres)", example = "Mimamamemima321", minLength = 6, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Rol (ADMIN, EMPLOYEE, CLIENT)", example = "CLIENT", allowableValues = {"ADMIN", "EMPLOYEE", "CLIENT"})
    private String role;

    @NotBlank(message = "La dirección es obligatoria")
    @Schema(description = "Dirección", example = "Calle Asados 123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{9,15}", message = "Teléfono debe tener entre 9 y 15 dígitos")
    @Schema(description = "Teléfono (9-15 dígitos)", example = "912345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}