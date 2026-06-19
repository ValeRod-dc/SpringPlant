package com.example.ms_users.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Respuesta con datos de usuario")
public class UserResponseDTO {

    @Schema(description = "ID del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @Schema(description = "Nombre de usuario", example = "Fernando Carnaca Supremo I")
    private String username;

    @Schema(description = "Correo electrónico", example = "fer_carnaca@gmail.com")
    private String email;

    @Schema(description = "Rol (ADMIN, EMPLOYEE, CLIENT)", example = "CLIENT")
    private String role;

    @Schema(description = "Dirección", example = "Calle Asados 123")
    private String address;

    @Schema(description = "Teléfono", example = "912345678")
    private String phone;

    @Schema(description = "Fecha de creación", example = "2026-01-15T12:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}