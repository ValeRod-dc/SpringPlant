package com.example.ms_notification.controller;

import com.example.ms_notification.dto.NotificationResponseDTO;
import com.example.ms_notification.exception.NotificationNotFoundException;
import com.example.ms_notification.model.enums.NotificationChannel;
import com.example.ms_notification.model.enums.NotificationStatus;
import com.example.ms_notification.model.enums.NotificationType;
import com.example.ms_notification.security.jwt.JwtService;
import com.example.ms_notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtService jwtService;

    private NotificationResponseDTO buildResponseDTO() {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(1L);
        dto.setUserId(5L);
        dto.setType(NotificationType.EMAIL);
        dto.setChannel(NotificationChannel.ORDER);
        dto.setMessage("Tu orden #3 ha sido confirmada y está en proceso de envío");
        dto.setStatus(NotificationStatus.PENDING);
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornarTodasLasNotificaciones() throws Exception {
        // Given
        when(notificationService.getAll()).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].message").exists());

        verify(notificationService).getAll();
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornarNotificacionPorId() throws Exception {
        // Given
        when(notificationService.getById(1L)).thenReturn(buildResponseDTO());

        // When / Then
        mockMvc.perform(get("/api/v1/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(5));

        verify(notificationService).getById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornar404CuandoNotificacionNoExiste() throws Exception {
        // Given
        when(notificationService.getById(99L))
                .thenThrow(new NotificationNotFoundException("Notificación no encontrada con id: 99"));

        // When / Then
        mockMvc.perform(get("/api/v1/notifications/99"))
                .andExpect(status().isNotFound());

        verify(notificationService).getById(99L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarNotificacionesPorUsuario() throws Exception {
        // Given
        when(notificationService.getByUserId(5L)).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/v1/notifications/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(5));

        verify(notificationService).getByUserId(5L);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornarNotificacionesPorEstado() throws Exception {
        // Given
        when(notificationService.getByStatus(NotificationStatus.PENDING)).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/v1/notifications/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(notificationService).getByStatus(NotificationStatus.PENDING);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaCrearNotificacion() throws Exception {
        // Given
        when(notificationService.save(any())).thenReturn(buildResponseDTO());

        String json = """
                {
                    "userId": 5,
                    "type": "EMAIL",
                    "channel": "ORDER",
                    "message": "Tu orden #3 ha sido confirmada y está en proceso de envío"
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(5));

        verify(notificationService).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {
        // Given
        String json = """
                {
                    "userId": 5
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaMarcarNotificacionComoEnviada() throws Exception {
        // Given
        NotificationResponseDTO enviado = buildResponseDTO();
        enviado.setStatus(NotificationStatus.SENT);
        when(notificationService.markAsSent(1L)).thenReturn(enviado);

        // When / Then
        mockMvc.perform(patch("/api/v1/notifications/1/sent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(notificationService).markAsSent(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaMarcarNotificacionComoFallida() throws Exception {
        // Given
        NotificationResponseDTO fallido = buildResponseDTO();
        fallido.setStatus(NotificationStatus.FAILED);
        when(notificationService.markAsFailed(1L)).thenReturn(fallido);

        // When / Then
        mockMvc.perform(patch("/api/v1/notifications/1/failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));

        verify(notificationService).markAsFailed(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaEliminarNotificacion() throws Exception {
        // Given
        when(notificationService.delete(1L)).thenReturn("Notificación con id 1 eliminada correctamente");

        // When / Then
        mockMvc.perform(delete("/api/v1/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificación con id 1 eliminada correctamente"));

        verify(notificationService).delete(1L);
    }
}
