package com.example.inventory.controller;

import com.example.inventory.dto.request.InventoryMovementRequestDTO;
import com.example.inventory.dto.response.InventoryMovementResponseDTO;
import com.example.inventory.service.InventoryMovementService;
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
@RequestMapping("/api/inventory-movements")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Inventario", description = "Gestión de movimientos de entrada y salida del inventario")
@SecurityRequirement(name = "bearerAuth")
public class InventoryMovementController {

    private final InventoryMovementService movementService;


    @Operation(
            summary = "Obtener todos los movimientos",
            description = "Retorna la lista completa de movimientos de inventario. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryMovementResponseDTO>> getAll() {
        return ResponseEntity.ok(movementService.getAll());
    }

    @Operation(
            summary = "Obtener movimiento por ID",
            description = "Busca y retorna un movimiento de inventario según su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InventoryMovementResponseDTO> getById(
            @Parameter(description = "ID del movimiento a buscar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(movementService.getById(id));
    }

    @Operation(
            summary = "Obtener movimientos por inventario",
            description = "Retorna todos los movimientos asociados a un registro de inventario específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/inventory/{inventoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryMovementResponseDTO>> getByInventoryId(
            @Parameter(description = "ID del inventario a consultar", example = "3")
            @PathVariable Long inventoryId) {
        return ResponseEntity.ok(movementService.getByInventoryId(inventoryId));
    }

    @Operation(
            summary = "Registrar movimiento",
            description = "Crea un nuevo movimiento de inventario (entrada o salida). Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movimiento registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InventoryMovementResponseDTO> save(
            @Valid @RequestBody InventoryMovementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.save(dto));
    }

    @Operation(
            summary = "Eliminar movimiento",
            description = "Elimina un movimiento de inventario por su ID. Solo accesible para ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimiento eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(
            @Parameter(description = "ID del movimiento a eliminar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(movementService.delete(id));
    }
}