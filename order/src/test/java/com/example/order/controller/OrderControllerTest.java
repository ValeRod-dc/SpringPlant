package com.example.order.controller;

import com.example.order.exception.OrderNotFoundException;
import com.example.order.model.Order;
import com.example.order.model.OrderItem;
import com.example.order.model.enums.OrderStatus;
import com.example.order.security.jwt.JwtService;
import com.example.order.service.CustomUserDetailsService;
import com.example.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Order buildOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setClientId(5L);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus("PENDING");
        order.setTotalAmount(15990.0);
        order.setItems(new ArrayList<>());
        return order;
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaCrearOrden() throws Exception {
        // Given
        when(orderService.createOrder(any(Order.class))).thenReturn(buildOrder());

        String json = """
                {
                    "clientId": 5,
                    "items": [
                        { "productId": 10, "quantity": 2 }
                    ]
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientId").value(5));

        verify(orderService).createOrder(any(Order.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornarTodasLasOrdenes() throws Exception {
        // Given
        when(orderService.getAllOrders()).thenReturn(List.of(buildOrder()));

        // When / Then
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(orderService).getAllOrders();
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornarOrdenesPorEstado() throws Exception {
        // Given
        when(orderService.getOrdersByStatus(OrderStatus.PENDING)).thenReturn(List.of(buildOrder()));

        // When / Then
        mockMvc.perform(get("/api/v1/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(orderService).getOrdersByStatus(OrderStatus.PENDING);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarOrdenesPorCliente() throws Exception {
        // Given
        when(orderService.getOrdersByClientId(5L)).thenReturn(List.of(buildOrder()));

        // When / Then
        mockMvc.perform(get("/api/v1/orders/client/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value(5));

        verify(orderService).getOrdersByClientId(5L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarOrdenPorId() throws Exception {
        // Given
        when(orderService.getOrderById(1L)).thenReturn(buildOrder());

        // When / Then
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).getOrderById(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornar404CuandoOrdenNoExiste() throws Exception {
        // Given
        when(orderService.getOrderById(99L)).thenThrow(new OrderNotFoundException("Orden no encontrada con el id: 99"));

        // When / Then
        mockMvc.perform(get("/api/v1/orders/99"))
                .andExpect(status().isNotFound());

        verify(orderService).getOrderById(99L);
    }

    @Test
    void deberiaVerificarExistenciaDeOrdenSinAutenticacion() throws Exception {
        // Given
        when(orderService.getOrderById(1L)).thenReturn(buildOrder());

        // When / Then
        mockMvc.perform(get("/api/v1/orders/exists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).getOrderById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaActualizarOrden() throws Exception {
        // Given
        Order actualizada = buildOrder();
        actualizada.setPaymentId("PAY-123456789");
        when(orderService.updateOrder(eq(1L), any(Order.class))).thenReturn(actualizada);

        String json = """
                {
                    "paymentId": "PAY-123456789"
                }
                """;

        // When / Then
        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-123456789"));

        verify(orderService).updateOrder(eq(1L), any(Order.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaCambiarEstadoDeOrden() throws Exception {
        // Given
        Order actualizada = buildOrder();
        actualizada.setStatus(OrderStatus.SHIPPED);
        when(orderService.changeOrderStatus(1L, OrderStatus.SHIPPED)).thenReturn(actualizada);

        // When / Then
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        verify(orderService).changeOrderStatus(1L, OrderStatus.SHIPPED);
    }

    @Test
    void deberiaActualizarEstadoDePagoSinAutenticacion() throws Exception {
        // Given
        Order actualizada = buildOrder();
        actualizada.setPaymentStatus("PAID");
        actualizada.setStatus(OrderStatus.PAID);
        when(orderService.updatePaymentStatus(1L, "PAID")).thenReturn(actualizada);

        // When / Then
        mockMvc.perform(patch("/api/v1/orders/1/payment-status")
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        verify(orderService).updatePaymentStatus(1L, "PAID");
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornar400CuandoArgumentoEsInvalido() throws Exception {
        // Given
        when(orderService.createOrder(any(Order.class)))
                .thenThrow(new IllegalArgumentException("Stock insuficiente para el producto: Monstera deliciosa"));

        String json = """
                {
                    "clientId": 5,
                    "items": [
                        { "productId": 10, "quantity": 999 }
                    ]
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Solicitud inválida"));

        verify(orderService).createOrder(any(Order.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaEliminarOrden() throws Exception {
        // Given
        when(orderService.deleteOrder(1L)).thenReturn("Order successfully deleted");

        // When / Then
        mockMvc.perform(delete("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order successfully deleted"));

        verify(orderService).deleteOrder(1L);
    }
}
