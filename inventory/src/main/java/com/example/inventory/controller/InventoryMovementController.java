package com.example.inventory.controller;

import com.example.inventory.dto.request.InventoryMovementRequestDTO;
import com.example.inventory.dto.response.InventoryMovementResponseDTO;
import com.example.inventory.service.InventoryMovementService;
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
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryMovementResponseDTO>> getAll() {
        return ResponseEntity.ok(movementService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InventoryMovementResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(movementService.getById(id));
    }

    @GetMapping("/inventory/{inventoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryMovementResponseDTO>> getByInventoryId(@PathVariable Long inventoryId) {
        return ResponseEntity.ok(movementService.getByInventoryId(inventoryId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InventoryMovementResponseDTO> save(@Valid @RequestBody InventoryMovementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.save(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return ResponseEntity.ok(movementService.delete(id));
    }
}