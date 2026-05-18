package com.example.inventory.service;

import com.example.inventory.client.ProductClient;
import com.example.inventory.dto.ProductDto;
import com.example.inventory.dto.UserDTO;
import com.example.inventory.dto.request.InventoryRequestDTO;
import com.example.inventory.dto.response.InventoryResponseDTO;
import com.example.inventory.exception.InventoryNotFoundException;
import com.example.inventory.model.Inventory;
import com.example.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.inventory.client.UserClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository repository;
    private final ProductClient productClient;
    private final UserClient userClient;

    public List<InventoryResponseDTO> getAll() {
        log.info("Obteniendo todos los inventarios");
        return repository.findAll().stream().map(this::mapToDTO).toList();
    }

    public InventoryResponseDTO getById(Long id) {
        log.info("Buscando inventario con id: {}", id);
        return mapToDTO(repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Inventario no encontrado con id: {}", id);
                    return new InventoryNotFoundException("Inventario no encontrado con el id: " + id);
                }));
    }

    public InventoryResponseDTO getByProductId(Long productId) {
        log.info("Buscando inventario para productId: {}", productId);
        return mapToDTO(repository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.warn("Inventario no encontrado para productId: {}", productId);
                    return new InventoryNotFoundException("Inventario no encontrado para el producto con id: " + productId);
                }));
    }

    public UserDTO findUserByUsername(String username) {
        log.debug("Buscando usuario por username: {}", username);
        // Ajusta según la definición real de UserClient
        return userClient.getUserByUsername(username).getBody();
    }

    public InventoryResponseDTO save(InventoryRequestDTO dto) {
        log.info("Creando inventario para productId: {}", dto.getProductId());
        ProductDto product = productClient.getProductById(dto.getProductId()).getBody();

        if (product == null) {
            log.warn("Producto con id {} no existe en el catálogo", dto.getProductId());
            throw new IllegalArgumentException("El producto con id " + dto.getProductId() + " no existe.");
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(dto.getProductId());
        inventory.setQuantityAvailable(dto.getQuantityAvailable());
        inventory.setQuantityReserved(dto.getQuantityReserved());
        inventory.setStoreLocation(dto.getStoreLocation());
        InventoryResponseDTO result = mapToDTO(repository.save(inventory));
        log.info("Inventario creado con id: {}", result.getId());
        return result;
    }

    public InventoryResponseDTO update(Long id, InventoryRequestDTO dto) {
        log.info("Actualizando inventario con id: {}", id);
        Inventory inventory = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Inventario no encontrado con id: {}", id);
                    return new InventoryNotFoundException("Inventario no encontrado con el id: " + id);
                });

        inventory.setQuantityAvailable(dto.getQuantityAvailable());
        inventory.setQuantityReserved(dto.getQuantityReserved());
        inventory.setStoreLocation(dto.getStoreLocation());

        InventoryResponseDTO result = mapToDTO(repository.save(inventory));
        log.info("Inventario actualizado con id: {}", result.getId());
        return result;
    }

    public String delete(Long id) {
        log.info("Eliminando inventario con id: {}", id);
        Inventory inventory = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Inventario no encontrado con id: {}", id);
                    return new InventoryNotFoundException("Inventario no encontrado con el id: " + id);
                });
        repository.delete(inventory);
        log.info("Inventario eliminado con id: {}", id);
        return "Inventario eliminado correctamente.";
    }

    private InventoryResponseDTO mapToDTO(Inventory inventory) {
        InventoryResponseDTO dto = new InventoryResponseDTO();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProductId());
        dto.setQuantityAvailable(inventory.getQuantityAvailable());
        dto.setQuantityReserved(inventory.getQuantityReserved());
        dto.setStoreLocation(inventory.getStoreLocation());
        dto.setLastRestockedAt(inventory.getLastRestockedAt());
        return dto;
    }
}
