package com.example.ms_notification.service;

import com.example.ms_notification.dto.NotificationRequestDTO;
import com.example.ms_notification.dto.NotificationResponseDTO;
import com.example.ms_notification.exception.NotificationNotFoundException;
import com.example.ms_notification.model.Notification;
import com.example.ms_notification.model.enums.NotificationStatus;
import com.example.ms_notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getAll() {
        log.info("Obteniendo todas las notificaciones");
        List<NotificationResponseDTO> result = repository.findAll().stream().map(this::toDTO).toList();
        log.info("Total notificaciones encontradas: {}", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public NotificationResponseDTO getById(Long id) {
        log.info("Buscando notificación con id: {}", id);
        Notification n = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Notificación no encontrada con id: {}", id);
                    return new NotificationNotFoundException("Notificación no encontrada con id: " + id);
                });
        return toDTO(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getByUserId(Long userId) {
        log.info("Buscando notificaciones del usuario con id: {}", userId);
        List<NotificationResponseDTO> result = repository.findByUserId(userId).stream().map(this::toDTO).toList();
        log.info("Notificaciones encontradas para usuario {}: {}", userId, result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getByStatus(NotificationStatus status) {
        log.info("Buscando notificaciones con estado: {}", status);
        List<NotificationResponseDTO> result = repository.findByStatus(status).stream().map(this::toDTO).toList();
        log.info("Notificaciones encontradas con estado {}: {}", status, result.size());
        return result;
    }

    @Transactional
    public NotificationResponseDTO save(NotificationRequestDTO dto) {
        log.info("Creando notificación para userId: {}, tipo: {}, canal: {}", dto.getUserId(), dto.getType(), dto.getChannel());
        Notification n = new Notification();
        n.setUserId(dto.getUserId());
        n.setType(dto.getType());
        n.setChannel(dto.getChannel());
        n.setMessage(dto.getMessage());
        Notification saved = repository.save(n);
        log.info("Notificación creada con id: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public NotificationResponseDTO markAsSent(Long id) {
        log.info("Marcando notificación {} como SENT", id);
        Notification n = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Notificación no encontrada con id: {}", id);
                    return new NotificationNotFoundException("Notificación no encontrada con id: " + id);
                });
        n.setStatus(NotificationStatus.SENT);
        n.setSentAt(LocalDateTime.now());
        return toDTO(repository.save(n));
    }

    @Transactional
    public NotificationResponseDTO markAsFailed(Long id) {
        log.info("Marcando notificación {} como FAILED", id);
        Notification n = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Notificación no encontrada con id: {}", id);
                    return new NotificationNotFoundException("Notificación no encontrada con id: " + id);
                });
        n.setStatus(NotificationStatus.FAILED);
        return toDTO(repository.save(n));
    }

    @Transactional
    public String delete(Long id) {
        log.info("Eliminando notificación con id: {}", id);
        repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Notificación no encontrada con id: {}", id);
                    return new NotificationNotFoundException("Notificación no encontrada con id: " + id);
                });
        repository.deleteById(id);
        log.info("Notificación {} eliminada correctamente", id);
        return "Notificación con id " + id + " eliminada correctamente";
    }

    private NotificationResponseDTO toDTO(Notification n) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(n.getId());
        dto.setUserId(n.getUserId());
        dto.setType(n.getType());
        dto.setChannel(n.getChannel());
        dto.setMessage(n.getMessage());
        dto.setStatus(n.getStatus());
        dto.setSentAt(n.getSentAt());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
