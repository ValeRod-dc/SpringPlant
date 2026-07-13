package com.example.ms_order.service;

import com.example.ms_order.client.ProductClient;
import com.example.ms_order.dto.ProductDto;
import com.example.ms_order.exception.OrderNotFoundException;
import com.example.ms_order.model.Order;
import com.example.ms_order.model.OrderItem;
import com.example.ms_order.model.enums.OrderStatus;
import com.example.ms_order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Transactional
    public Order createOrder(Order order) {
        log.info("Creando orden para clientId: {}", order.getClientId());
        double total = 0.0;

        for (OrderItem item : order.getItems()) {
            log.info("Validando producto id: {} con cantidad: {}", item.getProductId(), item.getQuantity());

            ProductDto product = productClient.getProductById(item.getProductId()).getBody();

            if (product == null) {
                log.warn("Producto con id {} no encontrado", item.getProductId());
                throw new IllegalArgumentException("Producto con ID " + item.getProductId() + " no encontrado.");
            }

            if (product.getStock() < item.getQuantity()) {
                log.warn("Stock insuficiente para producto: {} (disponible: {}, solicitado: {})",
                        product.getNombre(), product.getStock(), item.getQuantity());
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + product.getNombre());
            }

            item.setPrice(product.getPrecio());
            item.setOrder(order);
            total += item.getPrice() * item.getQuantity();
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("Orden creada con id: {} y total: ${}", saved.getId(), saved.getTotalAmount());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        log.info("Obteniendo todas las órdenes");
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        log.info("Buscando órdenes por estado: {}", status);
        return orderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByClientId(Long clientId) {
        log.info("Buscando órdenes del cliente id: {}", clientId);
        return orderRepository.findByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        log.info("Buscando orden con id: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Orden no encontrada con id: {}", id);
                    return new OrderNotFoundException("Orden no encontrada con el id: " + id);
                });
    }

    public boolean existsById(Long id) {
        log.debug("Verificando existencia de orden con ID: {}", id);
        return orderRepository.existsById(id);
    }

    @Transactional
    public Order updateOrder(Long id, Order orderData) {
        log.info("Actualizando orden con id: {}", id);
        Order existingOrder = getOrderById(id);
        //existingOrder.setShippingAddress(orderData.getShippingAddress());
        existingOrder.setPaymentId(orderData.getPaymentId());
        existingOrder.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(existingOrder);
        log.info("Orden actualizada con id: {}", updated.getId());
        return updated;
    }

    @Transactional
    public Order changeOrderStatus(Long id, OrderStatus newStatus) {
        log.info("Cambiando estado de orden id: {} a {}", id, newStatus);
        Order order = getOrderById(id);
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public Order updatePaymentStatus(Long id, String paymentStatus) {
        log.info("Actualizando estado de pago de orden id: {} a {}", id, paymentStatus);
        Order order = getOrderById(id);
        order.setPaymentStatus(paymentStatus);

        if ("PAID".equals(paymentStatus)) {
            order.setStatus(OrderStatus.PAID);
            log.info("Orden {} marcada como PAID", id);
        } else if ("PAYMENT_FAILED".equals(paymentStatus)) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            log.warn("Orden {} marcada como PAYMENT_FAILED", id);
        }

        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public String deleteOrder(Long id) {
        log.info("Eliminando orden con id: {}", id);
        Order order = getOrderById(id);
        orderRepository.delete(order);
        log.info("Orden eliminada con id: {}", id);
        return "Order successfully deleted";
    }
}