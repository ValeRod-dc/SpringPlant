package com.example.ms_product.model;

import com.example.ms_product.model.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "product")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad que representa a un producto registrado en el sistema")
public class Product extends RepresentationModel<Product> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del producto",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(
            description = "Nombre del producto",
            example = "Monstera deliciosa"
    )
    private String name;

    @Column(length = 500)
    @Schema(
            description = "Descripción detallada del producto",
            example = "Planta tropical de interior, ideal para espacios con luz indirecta"
    )
    private String description;

    @Column(nullable = false)
    @Schema(
            description = "Precio del producto en pesos chilenos",
            example = "15990.0"
    )
    private Double price;

    @Column(nullable = false)
    @Schema(
            description = "Cantidad disponible en inventario",
            example = "50"
    )
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Nivel de cuidado requerido por la planta",
            example = "EASY",
            allowableValues = {"EASY", "MEDIUM", "HARD"}
    )
    private CareLevel careLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Categoría a la que pertenece el producto",
            example = "INDOOR",
            allowableValues = {"INDOOR", "OUTDOOR", "SUCCULENT", "CACTUS"}
    )
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Estado actual del producto en la tienda",
            example = "AVAILABLE",
            allowableValues = {"AVAILABLE", "OUT_OF_STOCK", "DISCONTINUED"}
    )
    private ProductStatus productStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Frecuencia de riego recomendada",
            example = "WEEKLY",
            allowableValues = {"DAILY", "WEEKLY", "BIWEEKLY", "MONTHLY"}
    )
    private WateringFrequency wateringFrequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Tamaño de la planta",
            example = "MEDIUM",
            allowableValues = {"SMALL", "MEDIUM", "LARGE", "EXTRA_LARGE"}
    )
    private Size size;

    @Schema(
            description = "Fecha y hora de creación del registro",
            example = "2026-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Fecha y hora de la última actualización",
            example = "2026-01-20T14:45:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}