package com.example.ms_review.service;

import com.example.ms_review.client.OrderClient;
import com.example.ms_review.client.ProductClient;
import com.example.ms_review.dto.OrderDto;
import com.example.ms_review.dto.ProductDto;
import com.example.ms_review.dto.request.ReviewRequestDTO;
import com.example.ms_review.dto.response.ReviewResponseDTO;
import com.example.ms_review.exception.ReviewNotFoundException;
import com.example.ms_review.model.Review;
import com.example.ms_review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository repository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderClient orderClient;

    @InjectMocks
    private ReviewService service;

    private Review buildReview() {
        Review review = new Review();
        review.setId(1L);
        review.setUserId(5L);
        review.setProductId(10L);
        review.setOrderId(3L);
        review.setRating(4);
        review.setComment("Excelente planta, llegó en perfectas condiciones");
        review.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        return review;
    }

    private ReviewRequestDTO buildRequestDTO() {
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setUserId(5L);
        dto.setProductId(10L);
        dto.setOrderId(3L);
        dto.setRating(4);
        dto.setComment("Excelente planta, llegó en perfectas condiciones");
        return dto;
    }

    private ProductDto buildProductDto() {
        ProductDto productDto = new ProductDto();
        productDto.setId(10L);
        productDto.setName("Monstera deliciosa");
        productDto.setPrice(15990.0);
        productDto.setStock(10);
        productDto.setProductStatus("ACTIVE");
        return productDto;
    }

    private OrderDto buildOrderDto() {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(3L);
        orderDto.setClientId(5L);
        orderDto.setStatus("COMPLETED");
        return orderDto;
    }

    @Test
    void deberiaRetornarTodasLasReviews() {
        // Given
        Mockito.when(repository.findAll()).thenReturn(List.of(buildReview()));

        // When
        List<ReviewResponseDTO> resultado = service.getAll();

        // Then
        assertEquals(1, resultado.size());
        assertEquals("Excelente planta, llegó en perfectas condiciones", resultado.get(0).getComment());
        verify(repository).findAll();
    }

    @Test
    void deberiaRetornarReviewCuandoExiste() {
        // Given
        Review review = buildReview();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(review));

        // When
        ReviewResponseDTO resultado = service.getById(1L);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals(4, resultado.getRating());
        verify(repository).findById(1L);
    }

    @Test
    void deberiaLanzarExcepcionCuandoReviewNoExiste() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ReviewNotFoundException.class, () -> service.getById(99L));
        verify(repository).findById(99L);
    }

    @Test
    void deberiaRetornarReviewsPorProductId() {
        // Given
        Mockito.when(repository.findByProductId(10L)).thenReturn(List.of(buildReview()));

        // When
        List<ReviewResponseDTO> resultado = service.getByProductId(10L);

        // Then
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getProductId().equals(10L));
        verify(repository, times(1)).findByProductId(10L);
    }

    @Test
    void deberiaRetornarReviewsPorUserId() {
        // Given
        Mockito.when(repository.findByUserId(5L)).thenReturn(List.of(buildReview()));

        // When
        List<ReviewResponseDTO> resultado = service.getByUserId(5L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(5L, resultado.get(0).getUserId());
        verify(repository).findByUserId(5L);
    }

    @Test
    void deberiaRetornarReviewsPorOrderId() {
        // Given
        Mockito.when(repository.findByOrderId(3L)).thenReturn(List.of(buildReview()));

        // When
        List<ReviewResponseDTO> resultado = service.getByOrderId(3L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(3L, resultado.get(0).getOrderId());
        verify(repository).findByOrderId(3L);
    }

    @Test
    void deberiaCrearReviewCuandoProductoYOrdenExisten() {
        // Given
        ReviewRequestDTO dto = buildRequestDTO();
        Review review = buildReview();

        Mockito.when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(buildProductDto()));
        Mockito.when(orderClient.getOrderById(3L)).thenReturn(ResponseEntity.ok(buildOrderDto()));
        Mockito.when(repository.save(any(Review.class))).thenReturn(review);

        // When
        ReviewResponseDTO resultado = service.save(dto);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals(4, resultado.getRating());
        verify(productClient).getProductById(10L);
        verify(orderClient).getOrderById(3L);
        verify(repository).save(any(Review.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoProductoNoExisteAlCrearReview() {
        // Given
        ReviewRequestDTO dto = buildRequestDTO();
        Mockito.when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(null));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        verify(productClient).getProductById(10L);
        verify(orderClient, never()).getOrderById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionCuandoOrdenNoExisteAlCrearReview() {
        // Given
        ReviewRequestDTO dto = buildRequestDTO();
        Mockito.when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(buildProductDto()));
        Mockito.when(orderClient.getOrderById(3L)).thenReturn(ResponseEntity.ok(null));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        verify(productClient).getProductById(10L);
        verify(orderClient).getOrderById(3L);
        verify(repository, never()).save(any());
    }

    @Test
    void deberiaEliminarReviewCuandoExiste() {
        // Given
        Mockito.when(repository.existsById(1L)).thenReturn(true);

        // When
        String resultado = service.delete(1L);

        // Then
        assertEquals("Review eliminada exitosamente", resultado);
        verify(repository).deleteById(1L);
    }

    @Test
    void noDeberiaEliminarReviewCuandoNoExiste() {
        // Given
        Mockito.when(repository.existsById(99L)).thenReturn(false);

        // When / Then
        assertThrows(ReviewNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).deleteById(any());
    }
}
