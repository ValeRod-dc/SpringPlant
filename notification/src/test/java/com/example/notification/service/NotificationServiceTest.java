package com.example.notification.service;

import com.example.notification.dto.NotificationRequestDTO;
import com.example.notification.dto.NotificationResponseDTO;
import com.example.notification.exception.NotificationNotFoundException;
import com.example.notification.model.Notification;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.model.enums.NotificationStatus;
import com.example.notification.model.enums.NotificationType;
import com.example.notification.repository.NotificationRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService service;

    private Notification buildNotification() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(5L);
        notification.setType(NotificationType.EMAIL);
        notification.setChannel(NotificationChannel.ORDER);
        notification.setMessage("Tu orden #3 ha sido confirmada y está en proceso de envío");
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private NotificationRequestDTO buildRequestDTO() {
        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setUserId(5L);
        dto.setType(NotificationType.EMAIL);
        dto.setChannel(NotificationChannel.ORDER);
        dto.setMessage("Tu orden #3 ha sido confirmada y está en proceso de envío");
        return dto;
    }

    @Test
    void deberiaRetornarTodasLasNotificaciones() {
        // Given
        Mockito.when(repository.findAll()).thenReturn(List.of(buildNotification()));

        // When
        List<NotificationResponseDTO> resultado = service.getAll();

        // Then
        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void deberiaRetornarNotificacionCuandoExiste() {
        // Given
        Notification notification = buildNotification();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(notification));

        // When
        NotificationResponseDTO resultado = service.getById(1L);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals(5L, resultado.getUserId());
        verify(repository).findById(1L);
    }

    @Test
    void deberiaLanzarExcepcionCuandoNotificacionNoExiste() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotificationNotFoundException.class, () -> service.getById(99L));
        verify(repository).findById(99L);
    }

    @Test
    void deberiaRetornarNotificacionesPorUsuario() {
        // Given
        Mockito.when(repository.findByUserId(5L)).thenReturn(List.of(buildNotification()));

        // When
        List<NotificationResponseDTO> resultado = service.getByUserId(5L);

        // Then
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getUserId().equals(5L));
        verify(repository, times(1)).findByUserId(5L);
    }

    @Test
    void deberiaRetornarNotificacionesPorEstado() {
        // Given
        Mockito.when(repository.findByStatus(NotificationStatus.PENDING)).thenReturn(List.of(buildNotification()));

        // When
        List<NotificationResponseDTO> resultado = service.getByStatus(NotificationStatus.PENDING);

        // Then
        assertEquals(1, resultado.size());
        verify(repository).findByStatus(NotificationStatus.PENDING);
    }

    @Test
    void deberiaCrearNotificacion() {
        // Given
        NotificationRequestDTO requestDTO = buildRequestDTO();
        Notification saved = buildNotification();
        Mockito.when(repository.save(any(Notification.class))).thenReturn(saved);

        // When
        NotificationResponseDTO resultado = service.save(requestDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(NotificationType.EMAIL, resultado.getType());
        verify(repository).save(any(Notification.class));
    }

    @Test
    void deberiaMarcarNotificacionComoEnviadaCuandoExiste() {
        // Given
        Notification notification = buildNotification();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(notification));
        Mockito.when(repository.save(any(Notification.class))).thenReturn(notification);

        // When
        NotificationResponseDTO resultado = service.markAsSent(1L);

        // Then
        assertEquals(NotificationStatus.SENT, resultado.getStatus());
        assertNotNull(resultado.getSentAt());
        verify(repository).save(notification);
    }

    @Test
    void deberiaLanzarExcepcionAlMarcarComoEnviadaCuandoNoExiste() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotificationNotFoundException.class, () -> service.markAsSent(99L));
        verify(repository, never()).save(any());
    }

    @Test
    void deberiaMarcarNotificacionComoFallidaCuandoExiste() {
        // Given
        Notification notification = buildNotification();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(notification));
        Mockito.when(repository.save(any(Notification.class))).thenReturn(notification);

        // When
        NotificationResponseDTO resultado = service.markAsFailed(1L);

        // Then
        assertEquals(NotificationStatus.FAILED, resultado.getStatus());
        verify(repository).save(notification);
    }

    @Test
    void deberiaLanzarExcepcionAlMarcarComoFallidaCuandoNoExiste() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotificationNotFoundException.class, () -> service.markAsFailed(99L));
        verify(repository, never()).save(any());
    }

    @Test
    void deberiaEliminarNotificacionCuandoExiste() {
        // Given
        Notification notification = buildNotification();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(notification));

        // When
        String resultado = service.delete(1L);

        // Then
        assertTrue(resultado.contains("eliminada correctamente"));
        verify(repository).deleteById(1L);
    }

    @Test
    void noDeberiaEliminarNotificacionCuandoNoExiste() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotificationNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).deleteById(any());
    }
}
