package com.example.ms_shipping.service;

import com.example.ms_shipping.client.OrderClient;
import com.example.ms_shipping.client.UserClient;
import com.example.ms_shipping.dto.OrderDTO;
import com.example.ms_shipping.dto.request.CreateShippingRequest;
import com.example.ms_shipping.dto.response.ShippingResponseDTO;
import com.example.ms_shipping.exception.custom.*;
import com.example.ms_shipping.model.Shipping;
import com.example.ms_shipping.model.ShippingStatus;
import com.example.ms_shipping.repository.ShippingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderClient orderClient;
    private final UserClient userClient;

    public Shipping findByIdOrThrow(Long shippingId) {
        log.debug("Buscando envío por ID: {}", shippingId);
        return shippingRepository.findById(shippingId)
                .orElseThrow(() -> {
                    log.warn("Envío no encontrado - ID: {}", shippingId);
                    return new ShippingNotFoundException("Envío no encontrado con ID: " + shippingId);
                });
    }

    public boolean shippingExistsByOrder(Long orderId) {
        log.debug("Verificando si existe envío para la orden: {}", orderId);
        return shippingRepository.existsByOrderId(orderId);
    }

    @Transactional
    public ShippingResponseDTO createShipping(String username, CreateShippingRequest request) {
        log.info("Creando envío - Usuario: {}, Orden: {}", username, request.getOrderId());

        // 1. Validar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // 2. Obtener y validar la orden
        OrderDTO order;
        try {
            order = orderClient.getOrderById(request.getOrderId());
        } catch (Exception e) {
            log.error("Error al obtener la orden: {}", e.getMessage());
            throw new OrderNotFoundException("Orden no encontrada con ID: " + request.getOrderId());
        }

        // 3. Validar que la orden existe y está pagada
        if (order == null) {
            throw new OrderNotFoundException("Orden no encontrada con ID: " + request.getOrderId());
        }

        if (!"PAID".equals(order.getPaymentStatus())) {
            log.warn("La orden {} no está pagada. Estado actual: {}", request.getOrderId(), order.getPaymentStatus());
            throw new OrderNotPaidException("La orden debe estar pagada para poder generar el envío. Estado actual: " + order.getPaymentStatus());
        }

        // 4. Validar que no exista un envío previo para esta orden
        if (shippingExistsByOrder(request.getOrderId())) {
            log.warn("Ya existe un envío para la orden: {}", request.getOrderId());
            throw new ShippingAlreadyExistsException("Ya existe un envío para la orden: " + request.getOrderId());
        }

        // 5. Crear el envío
        Shipping shipping = Shipping.builder()
                .orderId(request.getOrderId())
                .userId(order.getClientId())
                .address(request.getAddress())
                .status(ShippingStatus.PENDING)
                .trackingNumber(generateTrackingNumber())
                .build();

        Shipping saved = shippingRepository.save(shipping);
        log.info("Envío creado con id: {}, tracking: {}", saved.getShippingId(), saved.getTrackingNumber());

        return mapToResponseDTO(saved);
    }

    @Transactional
    public ShippingResponseDTO updateStatus(Long shippingId, ShippingStatus newStatus) {
        log.info("Actualizando estado del envío {} a {}", shippingId, newStatus);
        Shipping shipping = findByIdOrThrow(shippingId);

        ShippingStatus oldStatus = shipping.getStatus();
        shipping.setStatus(newStatus);

        if (newStatus == ShippingStatus.SHIPPED && oldStatus != ShippingStatus.SHIPPED) {
            shipping.setShippedAt(LocalDateTime.now());
            shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(5));
            log.debug("Fecha de envío y estimación actualizadas");
        } else if (newStatus == ShippingStatus.DELIVERED && oldStatus != ShippingStatus.DELIVERED) {
            shipping.setDeliveredAt(LocalDateTime.now());
            log.debug("Fecha de entrega registrada");
        }

        Shipping updated = shippingRepository.save(shipping);
        log.info("Estado actualizado para envío {}", shippingId);
        return mapToResponseDTO(updated);
    }

    public ShippingResponseDTO getShipping(Long id) {
        log.debug("Consultando envío por id: {}", id);
        Shipping shipping = findByIdOrThrow(id);
        return mapToResponseDTO(shipping);
    }

    public List<ShippingResponseDTO> getByOrder(Long orderId) {
        log.debug("Consultando envíos por orderId: {}", orderId);
        return shippingRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ShippingResponseDTO> getByUser(Long userId) {
        log.debug("Consultando envíos por userId: {}", userId);
        return shippingRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ShippingResponseDTO> getByStatus(ShippingStatus status) {
        log.debug("Consultando envíos por estado: {}", status);
        return shippingRepository.findByStatus(status).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private String generateTrackingNumber() {
        return "TRK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private ShippingResponseDTO mapToResponseDTO(Shipping shipping) {
        return ShippingResponseDTO.builder()
                .shippingId(shipping.getShippingId())
                .orderId(shipping.getOrderId())
                .userId(shipping.getUserId())
                .address(shipping.getAddress())
                .status(shipping.getStatus())
                .trackingNumber(shipping.getTrackingNumber())
                .createdAt(shipping.getCreatedAt())
                .shippedAt(shipping.getShippedAt())
                .estimatedDelivery(shipping.getEstimatedDelivery())
                .deliveredAt(shipping.getDeliveredAt())
                .build();
    }
}