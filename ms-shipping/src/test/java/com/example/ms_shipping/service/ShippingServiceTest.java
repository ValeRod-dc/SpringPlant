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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    //mocks = dependencias falsas
    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private OrderClient orderClient;

    @Mock
    private UserClient userClient;

    //crea instancia real del ShippingService e inyecta los mocks de arriba como si fueran sus dependencias reales
    @InjectMocks
    private ShippingService shippingService;

    private CreateShippingRequest validRequest;
    private OrderDTO validOrder;
    private Shipping savedShipping;

    //Prepara los datos de prueba una sola vez para cada test
    @BeforeEach
    void setUp() {

        //Crea objeto request, body - POST
        validRequest = new CreateShippingRequest();
        validRequest.setOrderId(2L);
        validRequest.setAddress("Av. Siempre Viva 123");

        //Crea una orden falsa que simula la respuesta del ms-order para así crear un envío
        validOrder = new OrderDTO();
        validOrder.setId(2L);
        validOrder.setClientId(102L);
        validOrder.setPaymentStatus("PAID");
        validOrder.setTotalAmount(125.50);

        //Crea el objeto Shipping que simula lo que devolvería la bd luego de guardar
        savedShipping = Shipping.builder()
                .shippingId(1L)
                .orderId(2L)
                .userId(102L)
                .address("Av. Siempre Viva 123")
                .status(ShippingStatus.PENDING)
                .trackingNumber("TRKABC123")
                .build();
    }

    // ========== TEST: createShipping ==========

    @Test
    void shouldCreateShippingWhenUserExistsAndOrderPaid() {
        // Given
        String username = "vale";
        when(userClient.userExists(username)).thenReturn(true);
        when(orderClient.getOrderById(2L)).thenReturn(validOrder);
        when(shippingRepository.existsByOrderId(2L)).thenReturn(false);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(savedShipping);

        // When
        ShippingResponseDTO result = shippingService.createShipping(username, validRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getShippingId());
        assertEquals(2L, result.getOrderId());
        assertEquals("TRKABC123", result.getTrackingNumber());
        assertEquals(ShippingStatus.PENDING, result.getStatus());

        verify(userClient).userExists(username);
        verify(orderClient).getOrderById(2L);
        verify(shippingRepository).existsByOrderId(2L);
        verify(shippingRepository).save(any(Shipping.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Given
        String username = "non_existing_user";
        when(userClient.userExists(username)).thenReturn(false);

        // When & Then
        assertThrows(UserNotFoundException.class, () ->
                shippingService.createShipping(username, validRequest)
        );

        verify(userClient).userExists(username);
        verify(orderClient, never()).getOrderById(anyLong());
        verify(shippingRepository, never()).save(any(Shipping.class));
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        // Given
        String username = "vale";
        when(userClient.userExists(username)).thenReturn(true);
        when(orderClient.getOrderById(2L)).thenThrow(new RuntimeException("Order not found"));

        // When & Then
        assertThrows(OrderNotFoundException.class, () ->
                shippingService.createShipping(username, validRequest)
        );

        verify(userClient).userExists(username);
        verify(orderClient).getOrderById(2L);
        verify(shippingRepository, never()).save(any(Shipping.class));
    }

    @Test
    void shouldThrowOrderNotPaidExceptionWhenOrderIsNotPaid() {
        // Given
        String username = "vale";
        validOrder.setPaymentStatus("PENDING");
        when(userClient.userExists(username)).thenReturn(true);
        when(orderClient.getOrderById(2L)).thenReturn(validOrder);

        // When & Then
        assertThrows(OrderNotPaidException.class, () ->
                shippingService.createShipping(username, validRequest)
        );

        verify(userClient).userExists(username);
        verify(orderClient).getOrderById(2L);
        verify(shippingRepository, never()).save(any(Shipping.class));
    }

    @Test
    void shouldThrowShippingAlreadyExistsExceptionWhenShippingAlreadyExists() {
        // Given
        String username = "vale";
        when(userClient.userExists(username)).thenReturn(true);
        when(orderClient.getOrderById(2L)).thenReturn(validOrder);
        when(shippingRepository.existsByOrderId(2L)).thenReturn(true);

        // When & Then
        assertThrows(ShippingAlreadyExistsException.class, () ->
                shippingService.createShipping(username, validRequest)
        );

        verify(userClient).userExists(username);
        verify(orderClient).getOrderById(2L);
        verify(shippingRepository).existsByOrderId(2L);
        verify(shippingRepository, never()).save(any(Shipping.class));
    }

    // ========== TEST: updateStatus ==========

    @Test
    void shouldUpdateShippingStatus() {
        // Given
        Long shippingId = 1L;
        Shipping existingShipping = Shipping.builder()
                .shippingId(shippingId)
                .orderId(2L)
                .status(ShippingStatus.PENDING)
                .build();

        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(existingShipping));
        when(shippingRepository.save(any(Shipping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ShippingResponseDTO result = shippingService.updateStatus(shippingId, ShippingStatus.SHIPPED);

        // Then
        assertNotNull(result);
        assertEquals(ShippingStatus.SHIPPED, result.getStatus());
        assertNotNull(result.getShippedAt());
        assertNotNull(result.getEstimatedDelivery());

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository).save(existingShipping);
    }

    @Test
    void shouldThrowShippingNotFoundExceptionWhenShippingNotFound() {
        // Given
        Long shippingId = 999L;
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ShippingNotFoundException.class, () ->
                shippingService.updateStatus(shippingId, ShippingStatus.SHIPPED)
        );

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any(Shipping.class));
    }

    // ========== TEST: findByIdOrThrow ==========

    @Test
    void shouldReturnShippingWhenExists() {
        // Given
        Long shippingId = 1L;
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(savedShipping));

        // When
        Shipping result = shippingService.findByIdOrThrow(shippingId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getShippingId());
        verify(shippingRepository).findById(shippingId);
    }

    @Test
    void shouldThrowShippingNotFoundExceptionWhenShippingDoesNotExist() {
        // Given
        Long shippingId = 999L;
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ShippingNotFoundException.class, () ->
                shippingService.findByIdOrThrow(shippingId)
        );

        verify(shippingRepository).findById(shippingId);
    }
}