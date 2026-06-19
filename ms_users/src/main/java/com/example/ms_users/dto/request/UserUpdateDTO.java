package com.example.ms_users.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
@Schema(description = "Solicitud para actualizar datos de usuario")
public class UserUpdateDTO {

    @Email(message = "Formato de email inválido")
    @Schema(description = "Nuevo correo (opcional)", example = "ahora_soy_vegano@gmail.com")
    private String email;

    @Schema(description = "Nueva dirección (opcional)", example = "Av. Comeplantas 321")
    private String address;

    @Schema(description = "Nuevo número de teléfono (opcional)", example = "987654321")
    private String phone;
}
