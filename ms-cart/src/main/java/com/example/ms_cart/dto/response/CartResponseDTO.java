package com.example.ms_cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Respuesta con el carrito completo")
public class CartResponseDTO extends RepresentationModel<CartResponseDTO> {

    @Schema(description = "ID interno del carrito", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long cartId;

    @Schema(description = "ID del usuario propietario", example = "1001", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @Schema(description = "Lista de productos en el carrito")
    private List<CartItemResponseDTO> items;

    @Schema(description = "Monto total del carrito", example = "25000", accessMode = Schema.AccessMode.READ_ONLY)
    private Double total;

    @Schema(description = "Fecha de creación del carrito", example = "2026-01-15T10:15:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Última fecha de modificación", example = "2026-01-16T12:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}