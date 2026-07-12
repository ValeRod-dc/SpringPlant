package com.example.ms_inventory.controller;

import com.example.ms_inventory.dto.request.InventoryRequestDTO;
import com.example.ms_inventory.dto.response.InventoryResponseDTO;
import com.example.ms_inventory.exception.InventoryNotFoundException;
import com.example.ms_inventory.security.jwt.JwtService;
import com.example.ms_inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService service;

    @MockitoBean
    private JwtService jwtService;

    private InventoryResponseDTO buildResponseDTO() {
        InventoryResponseDTO dto = new InventoryResponseDTO();
        dto.setId(1L);
        dto.setProductId(10L);
        dto.setQuantityAvailable(50);
        dto.setQuantityReserved(5);
        dto.setStoreLocation("Bodega Santiago Centro");
        dto.setLastRestockedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        return dto;
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarTodosLosInventarios() throws Exception {
        // Given
        when(service.getAll()).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].storeLocation").value("Bodega Santiago Centro"));

        verify(service).getAll();
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarInventarioPorId() throws Exception {
        // Given
        when(service.getById(1L)).thenReturn(buildResponseDTO());

        // When / Then
        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(10));

        verify(service).getById(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornar404CuandoInventarioNoExiste() throws Exception {
        // Given
        when(service.getById(99L)).thenThrow(new InventoryNotFoundException("Inventario no encontrado con el id: 99"));

        // When / Then
        mockMvc.perform(get("/api/inventory/99"))
                .andExpect(status().isNotFound());

        verify(service).getById(99L);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornarInventarioPorProductId() throws Exception {
        // Given
        when(service.getByProductId(10L)).thenReturn(buildResponseDTO());

        // When / Then
        mockMvc.perform(get("/api/inventory/product/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10));

        verify(service).getByProductId(10L);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornar404CuandoInventarioNoExisteParaProducto() throws Exception {
        // Given
        when(service.getByProductId(99L))
                .thenThrow(new InventoryNotFoundException("Inventario no encontrado para el producto con id: 99"));

        // When / Then
        mockMvc.perform(get("/api/inventory/product/99"))
                .andExpect(status().isNotFound());

        verify(service).getByProductId(99L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaCrearInventario() throws Exception {
        // Given
        when(service.save(any(InventoryRequestDTO.class))).thenReturn(buildResponseDTO());

        String json = """
                {
                    "productId": 10,
                    "quantityAvailable": 50,
                    "quantityReserved": 5,
                    "storeLocation": "Bodega Santiago Centro"
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/inventory")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.storeLocation").value("Bodega Santiago Centro"));

        verify(service).save(any(InventoryRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {
        // Given
        String json = """
                {
                    "productId": 10
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/inventory")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaActualizarInventario() throws Exception {
        // Given
        InventoryResponseDTO actualizado = buildResponseDTO();
        actualizado.setQuantityAvailable(80);
        when(service.update(eq(1L), any(InventoryRequestDTO.class))).thenReturn(actualizado);

        String json = """
                {
                    "productId": 10,
                    "quantityAvailable": 80,
                    "quantityReserved": 2,
                    "storeLocation": "Bodega Valparaiso"
                }
                """;

        // When / Then
        mockMvc.perform(put("/api/inventory/1")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityAvailable").value(80));

        verify(service).update(eq(1L), any(InventoryRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaEliminarInventario() throws Exception {
        // Given
        when(service.delete(1L)).thenReturn("Inventario eliminado correctamente.");

        // When / Then
        mockMvc.perform(delete("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventario eliminado correctamente."));

        verify(service).delete(1L);
    }
}
