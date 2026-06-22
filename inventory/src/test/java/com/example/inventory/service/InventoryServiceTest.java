package com.example.inventory.service;

import com.example.inventory.client.ProductClient;
import com.example.inventory.client.UserClient;
import com.example.inventory.dto.ProductDto;
import com.example.inventory.dto.UserDTO;
import com.example.inventory.dto.request.InventoryRequestDTO;
import com.example.inventory.dto.response.InventoryResponseDTO;
import com.example.inventory.exception.InventoryNotFoundException;
import com.example.inventory.model.Inventory;
import com.example.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

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
class InventoryServiceTest {

    @Mock
    private InventoryRepository repository;

    @Mock
    private ProductClient productClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private InventoryService service;

    private Inventory buildInventory() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProductId(10L);
        inventory.setQuantityAvailable(50);
        inventory.setQuantityReserved(5);
        inventory.setStoreLocation("Bodega Santiago Centro");
        inventory.setLastRestockedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        return inventory;
    }

    private InventoryRequestDTO buildRequestDTO() {
        return new InventoryRequestDTO(10L, 50, 5, "Bodega Santiago Centro");
    }

    private ProductDto buildProductDto() {
        ProductDto productDto = new ProductDto();
        productDto.setId(10L);
        productDto.setName("Monstera deliciosa");
        productDto.setPrice(15990.0);
        productDto.setStock(10);
        productDto.setProductStatus("ACTIVE");
        return productDto;
    }

    @Test
    void deberiaRetornarTodosLosInventarios() {
        // Given
        Mockito.when(repository.findAll()).thenReturn(List.of(buildInventory()));

        // When
        List<InventoryResponseDTO> resultado = service.getAll();

        // Then
        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void deberiaRetornarInventarioCuandoExiste() {
        // Given
        Inventory inventory = buildInventory();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(inventory));

        // When
        InventoryResponseDTO resultado = service.getById(1L);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals(10L, resultado.getProductId());
        verify(repository).findById(1L);
    }

    @Test
    void deberiaLanzarExcepcionCuandoInventarioNoExistePorId() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(InventoryNotFoundException.class, () -> service.getById(99L));
        verify(repository).findById(99L);
    }

    @Test
    void deberiaRetornarInventarioPorProductIdCuandoExiste() {
        // Given
        Inventory inventory = buildInventory();
        Mockito.when(repository.findByProductId(10L)).thenReturn(Optional.of(inventory));

        // When
        InventoryResponseDTO resultado = service.getByProductId(10L);

        // Then
        assertEquals(10L, resultado.getProductId());
        verify(repository).findByProductId(10L);
    }

    @Test
    void deberiaLanzarExcepcionCuandoInventarioNoExistePorProductId() {
        // Given
        Mockito.when(repository.findByProductId(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(InventoryNotFoundException.class, () -> service.getByProductId(99L));
        verify(repository).findByProductId(99L);
    }

    @Test
    void deberiaRetornarUsuarioPorUsername() {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1L);
        userDTO.setUsername("cata");
        Mockito.when(userClient.getUserByUsername("cata")).thenReturn(ResponseEntity.ok(userDTO));

        // When
        UserDTO resultado = service.findUserByUsername("cata");

        // Then
        assertEquals("cata", resultado.getUsername());
        verify(userClient).getUserByUsername("cata");
    }

    @Test
    void deberiaCrearInventarioCuandoProductoExiste() {
        // Given
        InventoryRequestDTO dto = buildRequestDTO();
        ProductDto productDto = buildProductDto();
        Inventory inventoryGuardado = buildInventory();

        Mockito.when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(productDto));
        Mockito.when(repository.save(any(Inventory.class))).thenReturn(inventoryGuardado);

        // When
        InventoryResponseDTO resultado = service.save(dto);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals(10L, resultado.getProductId());
        verify(repository).save(any(Inventory.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoProductoNoExisteAlCrear() {
        // Given
        InventoryRequestDTO dto = buildRequestDTO();
        Mockito.when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(null));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        verify(repository, never()).save(any(Inventory.class));
    }

    @Test
    void deberiaActualizarInventarioCuandoExiste() {
        // Given
        Inventory existente = buildInventory();
        InventoryRequestDTO dto = new InventoryRequestDTO(10L, 80, 2, "Bodega Valparaiso");

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(repository.save(any(Inventory.class))).thenReturn(existente);

        // When
        InventoryResponseDTO resultado = service.update(1L, dto);

        // Then
        assertEquals(80, resultado.getQuantityAvailable());
        assertEquals(2, resultado.getQuantityReserved());
        assertEquals("Bodega Valparaiso", resultado.getStoreLocation());
        verify(repository).save(existente);
    }

    @Test
    void deberiaLanzarExcepcionCuandoInventarioNoExisteAlActualizar() {
        // Given
        InventoryRequestDTO dto = buildRequestDTO();
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(InventoryNotFoundException.class, () -> service.update(99L, dto));
        verify(repository, never()).save(any(Inventory.class));
    }

    @Test
    void deberiaEliminarInventarioCuandoExiste() {
        // Given
        Inventory inventory = buildInventory();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(inventory));

        // When
        String resultado = service.delete(1L);

        // Then
        assertTrue(resultado.contains("eliminado"));
        verify(repository, times(1)).delete(inventory);
    }

    @Test
    void deberiaLanzarExcepcionCuandoInventarioNoExisteAlEliminar() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(InventoryNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).delete(any(Inventory.class));
    }
}
