package com.example.ms_discount.controller;

import com.example.ms_discount.dto.response.DiscountResult;
import com.example.ms_discount.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
@Tag(name = "Cupones", description = "Endpoints para que los usuarios usen y validen cupones de descuento")
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping("/use")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Usar cupón", description = "Aplica un cupón de descuento sobre el total del carrito y lo marca como usado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cupón aplicado correctamente"),
            @ApiResponse(responseCode = "400", description = "Cupón inválido, expirado o ya usado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<DiscountResult> useCoupon(@RequestParam String code,
                                                    @RequestParam Double cartTotal) {
        log.info("Usando cupón: {} con total carrito: ${}", code, cartTotal);
        DiscountResult result = discountService.useCoupon(code, cartTotal);
        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/validate/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    @Operation(summary = "Validar cupón", description = "Verifica si un cupón es válido para un total de carrito dado, sin marcarlo como usado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado de la validación"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado")
    })
    public ResponseEntity<DiscountResult> validateCoupon(@PathVariable String code,
                                                         @RequestParam(required = false) Double cartTotal) {
        log.info("Validando cupón: {} con total carrito: ${}", code, cartTotal != null ? cartTotal : 0);

        com.example.ms_discount.dto.request.ValidateCouponRequest request =
                new com.example.ms_discount.dto.request.ValidateCouponRequest();
        request.setCode(code);
        request.setCartTotal(cartTotal != null ? cartTotal : 0.0);

        DiscountResult result = discountService.validateCoupon(request);
        return ResponseEntity.ok(result);
    }
}