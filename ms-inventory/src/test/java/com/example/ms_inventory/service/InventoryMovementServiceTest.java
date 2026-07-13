package com.example.ms_inventory.service;

import com.example.ms_inventory.dto.request.InventoryMovementRequestDTO;
import com.example.ms_inventory.dto.response.InventoryMovementResponseDTO;
import com.example.ms_inventory.exception.InventoryNotFoundException;
import com.example.ms_inventory.model.InventoryMovement;
import com.example.ms_inventory.model.enums.MovementType;
import com.example.ms_inventory.repository.InventoryMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {

    @Mock
    private InventoryMovementRepository repository;

    @InjectMocks
    private InventoryMovementService service;

    private InventoryMovement buildMovement() {
        InventoryMovement movement = new InventoryMovement();
        movement.setId(1L);
        movement.setInventoryId(3L);
        movement.setType(MovementType.IN);
        movement.setQuantity(10);
        movement.setMovedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        return movement;
    }

    private InventoryMovementRequestDTO buildRequestDTO() {
        InventoryMovementRequestDTO dto = new InventoryMovementRequestDTO();
        dto.setInventoryId(3L);
        dto.setType(MovementType.IN);
        dto.setQuantity(10);
        return dto;
    }

    @Test
    void deberiaRetornarTodosLosMovimientos() {
        // Given
        Mockito.when(repository.findAll()).thenReturn(List.of(buildMovement()));

        // When
        List<InventoryMovementResponseDTO> resultado = service.getAll();

        // Then
        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void deberiaRetornarMovimientoCuandoExiste() {
        // Given
        InventoryMovement movement = buildMovement();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(movement));

        // When
        InventoryMovementResponseDTO resultado = service.getById(1L);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals(MovementType.IN, resultado.getType());
        verify(repository).findById(1L);
    }

    @Test
    void deberiaLanzarExcepcionCuandoMovimientoNoExiste() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(InventoryNotFoundException.class, () -> service.getById(99L));
        verify(repository).findById(99L);
    }

    @Test
    void deberiaRetornarMovimientosPorInventoryId() {
        // Given
        Mockito.when(repository.findByInventoryId(3L)).thenReturn(List.of(buildMovement()));

        // When
        List<InventoryMovementResponseDTO> resultado = service.getByInventoryId(3L);

        // Then
        assertEquals(1, resultado.size());
        verify(repository, times(1)).findByInventoryId(3L);
    }

    @Test
    void deberiaCrearMovimiento() {
        // Given
        InventoryMovementRequestDTO dto = buildRequestDTO();
        InventoryMovement guardado = buildMovement();
        Mockito.when(repository.save(any(InventoryMovement.class))).thenReturn(guardado);

        // When
        InventoryMovementResponseDTO resultado = service.save(dto);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals(3L, resultado.getInventoryId());
        verify(repository).save(any(InventoryMovement.class));
    }

    @Test
    void deberiaEliminarMovimientoCuandoExiste() {
        // Given
        InventoryMovement movement = buildMovement();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(movement));

        // When
        String resultado = service.delete(1L);

        // Then
        assertTrue(resultado.contains("Eliminado"));
        verify(repository).delete(movement);
    }

    @Test
    void noDeberiaEliminarMovimientoCuandoNoExiste() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(InventoryNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).delete(any(InventoryMovement.class));
    }
}
