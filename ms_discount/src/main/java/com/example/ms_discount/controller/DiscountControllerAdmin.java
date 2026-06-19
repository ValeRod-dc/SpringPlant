package com.example.ms_discount.controller;

import com.example.ms_discount.dto.request.CreateCouponRequest;
import com.example.ms_discount.dto.response.DiscountResponseDTO;
import com.example.ms_discount.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/discounts/admin")
@RequiredArgsConstructor
@Tag(name = "Cupones (Admin)", description = "Endpoints administrativos para crear y gestionar cupones de descuento")
public class DiscountControllerAdmin {

    private final DiscountService discountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear cupón", description = "Crea un nuevo cupón de descuento general.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cupón creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<DiscountResponseDTO> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        log.info("ADMIN - Creando cupón: {}", request.getCode());
        DiscountResponseDTO created = discountService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear cupón para un usuario", description = "Crea un cupón de descuento exclusivo para un usuario específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cupón creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<DiscountResponseDTO> createCouponForUser(@PathVariable String username,
                                                                   @Valid @RequestBody CreateCouponRequest request) {
        log.info("ADMIN - Creando cupón para usuario: {} con código: {}", username, request.getCode());
        DiscountResponseDTO created = discountService.createCouponForUser(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar cupón", description = "Desactiva un cupón existente por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cupón desactivado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado")
    })
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        log.info("ADMIN - Desactivando cupón con id: {}", id);
        discountService.deactivateCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar cupón", description = "Reactiva un cupón previamente desactivado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cupón activado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado")
    })
    public ResponseEntity<DiscountResponseDTO> activate(@PathVariable Long id) {
        log.info("ADMIN - Activando cupón con id: {}", id);
        discountService.activateCoupon(id);
        DiscountResponseDTO coupon = discountService.getCouponByCode(
                discountService.findByIdOrThrow(id).getCode());
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/exists/{id}")
    @Operation(summary = "Verificar existencia de cupón por ID", description = "Indica si existe un cupón con el ID dado. Uso interno entre microservicios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    })
    public ResponseEntity<Map<String, Boolean>> couponExists(@PathVariable Long id) {
        log.debug("Verificando existencia de cupón por ID: {}", id);
        boolean exists = discountService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}