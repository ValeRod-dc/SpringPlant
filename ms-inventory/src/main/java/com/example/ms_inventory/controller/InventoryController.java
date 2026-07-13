package com.example.ms_inventory.controller;

import com.example.ms_inventory.dto.request.InventoryRequestDTO;
import com.example.ms_inventory.dto.response.InventoryResponseDTO;
import com.example.ms_inventory.service.InventoryService;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Gestión del inventario de productos")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;


    @Operation(
            summary = "Obtener todo el inventario",
            description = "Retorna la lista completa de registros de inventario. Accesible para ADMIN, EMPLOYEE y CLIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inventario obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<InventoryResponseDTO>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @Operation(
            summary = "Obtener inventario por ID",
            description = "Busca y retorna un registro de inventario según su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario encontrado"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<InventoryResponseDTO> getById(
            @Parameter(description = "ID del inventario a buscar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }


    @Operation(
            summary = "Obtener inventario por producto",
            description = "Busca y retorna el registro de inventario asociado a un producto específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario encontrado"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado para el producto"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<InventoryResponseDTO> getByProductId(
            @Parameter(description = "ID del producto a consultar", example = "10")
            @PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.getByProductId(productId));
    }


    @Operation(
            summary = "Registrar inventario",
            description = "Crea un nuevo registro de inventario para un producto. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inventario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InventoryResponseDTO> save(
            @Valid @RequestBody InventoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.save(dto));
    }

    @Operation(
            summary = "Actualizar inventario",
            description = "Modifica los datos de un registro de inventario existente. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InventoryResponseDTO> update(
            @Parameter(description = "ID del inventario a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequestDTO dto) {
        return ResponseEntity.ok(inventoryService.update(id, dto));
    }

    @Operation(
            summary = "Eliminar inventario",
            description = "Elimina un registro de inventario por su ID. Solo accesible para ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(
            @Parameter(description = "ID del inventario a eliminar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.delete(id));
    }
}