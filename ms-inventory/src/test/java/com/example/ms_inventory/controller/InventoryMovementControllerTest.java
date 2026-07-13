package com.example.ms_inventory.controller;

import com.example.ms_inventory.dto.request.InventoryMovementRequestDTO;
import com.example.ms_inventory.dto.response.InventoryMovementResponseDTO;
import com.example.ms_inventory.exception.InventoryNotFoundException;
import com.example.ms_inventory.model.enums.MovementType;
import com.example.ms_inventory.security.jwt.JwtService;
import com.example.ms_inventory.service.InventoryMovementService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryMovementController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryMovementService movementService;

    @MockitoBean
    private JwtService jwtService;

    private InventoryMovementResponseDTO buildResponseDTO() {
        InventoryMovementResponseDTO dto = new InventoryMovementResponseDTO();
        dto.setId(1L);
        dto.setInventoryId(3L);
        dto.setType(MovementType.IN);
        dto.setQuantity(10);
        dto.setMovedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        return dto;
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornarTodosLosMovimientos() throws Exception {
        // Given
        when(movementService.getAll()).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/inventory-movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("IN"));

        verify(movementService).getAll();
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornarMovimientoPorId() throws Exception {
        // Given
        when(movementService.getById(1L)).thenReturn(buildResponseDTO());

        // When / Then
        mockMvc.perform(get("/api/inventory-movements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryId").value(3));

        verify(movementService).getById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornar404CuandoMovimientoNoExiste() throws Exception {
        // Given
        when(movementService.getById(99L))
                .thenThrow(new InventoryNotFoundException("Movimiento no encontrado con el id: 99"));

        // When / Then
        mockMvc.perform(get("/api/inventory-movements/99"))
                .andExpect(status().isNotFound());

        verify(movementService).getById(99L);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornarMovimientosPorInventoryId() throws Exception {
        // Given
        when(movementService.getByInventoryId(3L)).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/inventory-movements/inventory/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inventoryId").value(3));

        verify(movementService).getByInventoryId(3L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaCrearMovimiento() throws Exception {
        // Given
        when(movementService.save(any(InventoryMovementRequestDTO.class))).thenReturn(buildResponseDTO());

        String json = """
                {
                    "inventoryId": 3,
                    "type": "IN",
                    "quantity": 10
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/inventory-movements")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("IN"));

        verify(movementService).save(any(InventoryMovementRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {
        // Given
        String json = """
                {
                    "inventoryId": 3
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/inventory-movements")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaEliminarMovimiento() throws Exception {
        // Given
        when(movementService.delete(1L)).thenReturn("Eliminado correctamente.");

        // When / Then
        mockMvc.perform(delete("/api/inventory-movements/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Eliminado correctamente."));

        verify(movementService).delete(1L);
    }
}
