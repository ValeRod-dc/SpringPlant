package com.example.ms_users.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Schema(description = "Entidad que representa a un usuario registrado en el sistema.")
public class User extends RepresentationModel<User> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del usuario",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    @Schema(
            description = "Nombre de usuario.",
            example = "Fernando Carnaca Supremo I",
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    @Schema(description = "Correo electrónico (único)",
            example = "fer_carnaca@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Column(nullable = false)
    @Schema(description = "Contraseña (hash, no se devuelve)",
            accessMode = Schema.AccessMode.WRITE_ONLY,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Rol del usuario", example = "CLIENT",
            allowableValues = {"ADMIN", "EMPLOYEE", "CLIENT"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Role role;

    @Schema(description = "Dirección", example = "Calle Asados 321")
    private String address;

    @Schema(description = "Teléfono (9-15 dígitos",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String phone;

    @Column(name = "created_at")
    @Schema(description = "Fecha de creación de la cuenta",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}