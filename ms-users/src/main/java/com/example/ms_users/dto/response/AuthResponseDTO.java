package com.example.ms_users.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Respuesta de autenticación (login)")
public class AuthResponseDTO {

    @Schema(description = "Token JWT", example = "Contraseña_larga_y_segura_el_futuro_es_vegano...")
    private String token;

    @Schema(description = "Nombre de usuario", example = "Fernando Carnaca Supremo I")
    private String username;

    @Schema(description = "Rol del usuario", example = "CLIENT")
    private String role;

    @Schema(description = "Mensaje de respuesta", example = "Login exitoso")
    private String message;
}
