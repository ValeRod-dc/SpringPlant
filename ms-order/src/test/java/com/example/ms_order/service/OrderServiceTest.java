package com.example.ms_order.service;

import com.example.ms_order.client.ProductClient;
import com.example.ms_order.dto.ProductDto;
import com.example.ms_order.exception.OrderNotFoundException;
import com.example.ms_order.model.Order;
import com.example.ms_order.model.OrderItem;
import com.example.ms_order.model.enums.OrderStatus;
import com.example.ms_order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService service;

    private Order buildOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setClientId(5L);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus("PENDING");
        order.setTotalAmount(0.0);
        order.setItems(new ArrayList<>());
        return order;
    }

    private OrderItem buildItem(Long productId, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private ProductDto buildProductDto(Long id, String nombre, Double precio, Integer stock) {
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName(nombre);
        dto.setPrice(precio);
        dto.setStock(stock);
        return dto;
    }

    @Test
    void deberiaCrearOrdenCuandoProductosSonValidos() {
        // Given
        Order order = buildOrder();
        OrderItem item = buildItem(10L, 2);
        order.setItems(List.of(item));

        ProductDto productDto = buildProductDto(10L, "Monstera deliciosa", 15990.0, 10);
        Mockito.when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(productDto));
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order resultado = service.createOrder(order);

        // Then
        assertEquals(OrderStatus.PENDING, resultado.getStatus());
        assertEquals("PENDING", resultado.getPaymentStatus());
        verify(productClient).getProductById(10L);
        verify(orderRepository).save(order);
    }

    @Test
    void deberiaLanzarExcepcionCuandoProductoNoExisteAlCrearOrden() {
        // Given
        Order order = buildOrder();
        OrderItem item = buildItem(99L, 1);
        order.setItems(List.of(item));

        Mockito.when(productClient.getProductById(99L)).thenReturn(ResponseEntity.ok(null));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(order));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionCuandoStockEsInsuficiente() {
        // Given
        Order order = buildOrder();
        OrderItem item = buildItem(10L, 100);
        order.setItems(List.of(item));

        ProductDto productDto = buildProductDto(10L, "Monstera deliciosa", 15990.0, 10);
        Mockito.when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(productDto));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(order));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deberiaRetornarTodasLasOrdenes() {
        // Given
        Mockito.when(orderRepository.findAll()).thenReturn(List.of(buildOrder()));

        // When
        List<Order> resultado = service.getAllOrders();

        // Then
        assertEquals(1, resultado.size());
        verify(orderRepository).findAll();
    }

    @Test
    void deberiaRetornarOrdenesPorEstado() {
        // Given
        Mockito.when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(buildOrder()));

        // When
        List<Order> resultado = service.getOrdersByStatus(OrderStatus.PENDING);

        // Then
        assertEquals(1, resultado.size());
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
    }

    @Test
    void deberiaRetornarOrdenesPorCliente() {
        // Given
        Mockito.when(orderRepository.findByClientId(5L)).thenReturn(List.of(buildOrder()));

        // When
        List<Order> resultado = service.getOrdersByClientId(5L);

        // Then
        assertEquals(1, resultado.size());
        verify(orderRepository).findByClientId(5L);
    }

    @Test
    void deberiaRetornarOrdenCuandoExiste() {
        // Given
        Order order = buildOrder();
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        Order resultado = service.getOrderById(1L);

        // Then
        assertEquals(1L, resultado.getId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void deberiaLanzarExcepcionCuandoOrdenNoExiste() {
        // Given
        Mockito.when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(OrderNotFoundException.class, () -> service.getOrderById(99L));
        verify(orderRepository).findById(99L);
    }

    @Test
    void deberiaRetornarTrueCuandoOrdenExiste() {
        // Given
        Mockito.when(orderRepository.existsById(1L)).thenReturn(true);

        // When
        boolean resultado = service.existsById(1L);

        // Then
        assertTrue(resultado);
        verify(orderRepository).existsById(1L);
    }

    @Test
    void deberiaRetornarFalseCuandoOrdenNoExiste() {
        // Given
        Mockito.when(orderRepository.existsById(99L)).thenReturn(false);

        // When
        boolean resultado = service.existsById(99L);

        // Then
        assertEquals(false, resultado);
        verify(orderRepository).existsById(99L);
    }

    @Test
    void deberiaActualizarOrdenCuandoExiste() {
        // Given
        Order existente = buildOrder();
        Order nuevosDatos = buildOrder();
        nuevosDatos.setPaymentId("PAY-123456789");

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(existente);

        // When
        Order resultado = service.updateOrder(1L, nuevosDatos);

        // Then
        assertEquals("PAY-123456789", resultado.getPaymentId());
        verify(orderRepository).save(existente);
    }

    @Test
    void deberiaLanzarExcepcionAlActualizarOrdenInexistente() {
        // Given
        Mockito.when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(OrderNotFoundException.class, () -> service.updateOrder(99L, buildOrder()));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deberiaCambiarEstadoDeOrdenCuandoExiste() {
        // Given
        Order order = buildOrder();
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order resultado = service.changeOrderStatus(1L, OrderStatus.SHIPPED);

        // Then
        assertEquals(OrderStatus.SHIPPED, resultado.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void deberiaLanzarExcepcionAlCambiarEstadoDeOrdenInexistente() {
        // Given
        Mockito.when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(OrderNotFoundException.class, () -> service.changeOrderStatus(99L, OrderStatus.SHIPPED));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deberiaActualizarEstadoDePagoAPaid() {
        // Given
        Order order = buildOrder();
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order resultado = service.updatePaymentStatus(1L, "PAID");

        // Then
        assertEquals("PAID", resultado.getPaymentStatus());
        assertEquals(OrderStatus.PAID, resultado.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void deberiaActualizarEstadoDePagoAPaymentFailed() {
        // Given
        Order order = buildOrder();
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order resultado = service.updatePaymentStatus(1L, "PAYMENT_FAILED");

        // Then
        assertEquals("PAYMENT_FAILED", resultado.getPaymentStatus());
        assertEquals(OrderStatus.PAYMENT_FAILED, resultado.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void deberiaLanzarExcepcionAlActualizarEstadoDePagoDeOrdenInexistente() {
        // Given
        Mockito.when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(OrderNotFoundException.class, () -> service.updatePaymentStatus(99L, "PAID"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deberiaEliminarOrdenCuandoExiste() {
        // Given
        Order order = buildOrder();
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        String resultado = service.deleteOrder(1L);

        // Then
        assertEquals("Order successfully deleted", resultado);
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void noDeberiaEliminarOrdenCuandoNoExiste() {
        // Given
        Mockito.when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(OrderNotFoundException.class, () -> service.deleteOrder(99L));
        verify(orderRepository, never()).delete(any());
    }
}
