package com.example.ms_review.controller;

import com.example.ms_review.client.OrderClient;
import com.example.ms_review.client.ProductClient;
import com.example.ms_review.dto.request.ReviewRequestDTO;
import com.example.ms_review.dto.response.ReviewResponseDTO;
import com.example.ms_review.exception.ReviewNotFoundException;
import com.example.ms_review.security.jwt.JwtService;
import com.example.ms_review.service.ReviewService;
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

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService service;

    @MockitoBean
    private ProductClient productClient;

    @MockitoBean
    private OrderClient orderClient;

    @MockitoBean
    private JwtService jwtService;

    private ReviewResponseDTO buildResponseDTO() {
        return ReviewResponseDTO.builder()
                .id(1L)
                .userId(5L)
                .productId(10L)
                .orderId(3L)
                .rating(4)
                .comment("Excelente planta, llegó en perfectas condiciones")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornarTodasLasReviews() throws Exception {
        // Given
        when(service.getAll()).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].comment").value("Excelente planta, llegó en perfectas condiciones"));

        verify(service).getAll();
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarReviewPorId() throws Exception {
        // Given
        when(service.getById(1L)).thenReturn(buildResponseDTO());

        // When / Then
        mockMvc.perform(get("/api/v1/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(4));

        verify(service).getById(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornar404CuandoReviewNoExiste() throws Exception {
        // Given
        when(service.getById(99L)).thenThrow(new ReviewNotFoundException("Review no encontrada con id: 99"));

        // When / Then
        mockMvc.perform(get("/api/v1/reviews/99"))
                .andExpect(status().isNotFound());

        verify(service).getById(99L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarReviewsPorProductId() throws Exception {
        // Given
        when(service.getByProductId(10L)).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/v1/reviews/product/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10));

        verify(service).getByProductId(10L);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deberiaRetornarReviewsPorUserId() throws Exception {
        // Given
        when(service.getByUserId(5L)).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/v1/reviews/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(5));

        verify(service).getByUserId(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornarReviewsPorOrderId() throws Exception {
        // Given
        when(service.getByOrderId(3L)).thenReturn(List.of(buildResponseDTO()));

        // When / Then
        mockMvc.perform(get("/api/v1/reviews/order/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(3));

        verify(service).getByOrderId(3L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaCrearReview() throws Exception {
        // Given
        when(service.save(any(ReviewRequestDTO.class))).thenReturn(buildResponseDTO());

        String json = """
                {
                    "userId": 5,
                    "productId": 10,
                    "orderId": 3,
                    "rating": 4,
                    "comment": "Excelente planta, llegó en perfectas condiciones"
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(4));

        verify(service).save(any(ReviewRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {
        // Given
        String json = """
                {
                    "comment": "Sin datos obligatorios"
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaEliminarReview() throws Exception {
        // Given
        when(service.delete(1L)).thenReturn("Review eliminada exitosamente");

        // When / Then
        mockMvc.perform(delete("/api/v1/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Review eliminada exitosamente"));

        verify(service).delete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornar404AlEliminarReviewInexistente() throws Exception {
        // Given
        when(service.delete(99L)).thenThrow(new ReviewNotFoundException("Review no encontrada con id: 99"));

        // When / Then
        mockMvc.perform(delete("/api/v1/reviews/99"))
                .andExpect(status().isNotFound());

        verify(service).delete(99L);
    }
}
