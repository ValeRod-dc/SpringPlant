package com.example.ms_inventory.service;

import com.example.ms_inventory.dto.request.InventoryMovementRequestDTO;
import com.example.ms_inventory.dto.response.InventoryMovementResponseDTO;
import com.example.ms_inventory.exception.InventoryNotFoundException;
import com.example.ms_inventory.model.InventoryMovement;
import com.example.ms_inventory.repository.InventoryMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final InventoryMovementRepository repository;

    public List<InventoryMovementResponseDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDTO).toList();
    }

    public InventoryMovementResponseDTO getById(Long id) {
        return mapToDTO(repository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Movimiento no encontrado con el id: " + id)));
    }

    public List<InventoryMovementResponseDTO> getByInventoryId(Long inventoryId) {
        return repository.findByInventoryId(inventoryId).stream().map(this::mapToDTO).toList();
    }

    public InventoryMovementResponseDTO save(InventoryMovementRequestDTO dto) {
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryId(dto.getInventoryId());
        movement.setType(dto.getType());
        movement.setQuantity(dto.getQuantity());
        return mapToDTO(repository.save(movement));
    }

    public String delete(Long id) {
        InventoryMovement movement = repository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("No se encuentra con el id: " + id));
        repository.delete(movement);
        return "Eliminado correctamente.";
    }

    private InventoryMovementResponseDTO mapToDTO(InventoryMovement movement) {
        InventoryMovementResponseDTO dto = new InventoryMovementResponseDTO();
        dto.setId(movement.getId());
        dto.setInventoryId(movement.getInventoryId());
        dto.setType(movement.getType());
        dto.setQuantity(movement.getQuantity());
        dto.setMovedAt(movement.getMovedAt());
        return dto;
    }
}
