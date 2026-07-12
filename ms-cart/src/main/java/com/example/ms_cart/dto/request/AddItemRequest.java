package com.example.ms_cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Solicitud para agregar un producto al carrito")
public class AddItemRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    @Schema(description = "Identificador único del producto",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Schema(description = "Cantidad del producto a agregar",
            example = "2",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer quantity;
}