package com.example.ms_cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para generar una orden a partir del carrito actual")
public class CreateOrderFromCartDTO {

    @Schema(description = "ID del usuario", example = "1001")
    private Long userId;

    @Schema(description = "Lista de productos del carrito")
    private List<OrderItemDTO> items;

    @Schema(description = "Monto total de la orden", example = "20000")
    private Double totalAmount;

    @Schema(description = "Dirección de envío", example = "Av. Siempre viva 123, Santiago")
    private String shippingAddress;

    @Schema(description = "Código de descuento (opcional)", example = "BIENVENIDO10")
    private String couponCode;
}
