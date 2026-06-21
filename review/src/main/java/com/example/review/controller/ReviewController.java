package com.example.review.controller;

import com.example.review.dto.request.ReviewRequestDTO;
import com.example.review.dto.response.ReviewResponseDTO;
import com.example.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Gestión de reseñas de productos")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService service;

    @Operation(
            summary = "Obtener todas las reseñas",
            description = "Retorna la lista completa de reseñas. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reseñas obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<ReviewResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(
            summary = "Obtener reseña por ID",
            description = "Busca y retorna una reseña según su identificador único. Accesible para CLIENT, EMPLOYEE y ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña encontrada"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ReviewResponseDTO> getById(
            @Parameter(description = "ID de la reseña a buscar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(
            summary = "Obtener reseñas por producto",
            description = "Retorna todas las reseñas asociadas a un producto específico. Accesible para CLIENT, EMPLOYEE y ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reseñas obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<ReviewResponseDTO>> getByProductId(
            @Parameter(description = "ID del producto a consultar", example = "10")
            @PathVariable Long productId) {
        return ResponseEntity.ok(service.getByProductId(productId));
    }

    @Operation(
            summary = "Obtener reseñas por usuario",
            description = "Retorna todas las reseñas realizadas por un usuario específico. Accesible para CLIENT, EMPLOYEE y ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reseñas obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<ReviewResponseDTO>> getByUserId(
            @Parameter(description = "ID del usuario a consultar", example = "5")
            @PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @Operation(
            summary = "Obtener reseñas por orden",
            description = "Retorna todas las reseñas asociadas a una orden específica. Accesible para CLIENT, EMPLOYEE y ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reseñas obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<ReviewResponseDTO>> getByOrderId(
            @Parameter(description = "ID de la orden a consultar", example = "3")
            @PathVariable Long orderId) {
        return ResponseEntity.ok(service.getByOrderId(orderId));
    }

    @Operation(
            summary = "Crear reseña",
            description = "Registra una nueva reseña para un producto. Accesible para CLIENT y ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reseña creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ReviewResponseDTO> save(
            @RequestBody @Valid ReviewRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @Operation(
            summary = "Eliminar reseña",
            description = "Elimina una reseña del sistema por su ID. Solo accesible para ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(
            @Parameter(description = "ID de la reseña a eliminar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }
}