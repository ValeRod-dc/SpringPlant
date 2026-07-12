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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repository;
    private final ProductClient productClient;
    private final OrderClient orderClient;

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAll() {
        log.info("Obteniendo todas las reviews");
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponseDTO getById(Long id) {
        log.info("Buscando review con id: {}", id);
        Review review = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Review no encontrada con id: {}", id);
                    return new ReviewNotFoundException("Review no encontrada con id: " + id);
                });
        return toDTO(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getByProductId(Long productId) {
        log.info("Buscando reviews del producto id: {}", productId);
        return repository.findByProductId(productId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getByUserId(Long userId) {
        log.info("Buscando reviews del usuario id: {}", userId);
        return repository.findByUserId(userId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getByOrderId(Long orderId) {
        log.info("Buscando reviews de la orden id: {}", orderId);
        return repository.findByOrderId(orderId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public ReviewResponseDTO save(ReviewRequestDTO dto) {
        log.info("Creando review para productId: {} orderId: {}", dto.getProductId(), dto.getOrderId());

        ProductDto product = productClient.getProductById(dto.getProductId()).getBody();
        if (product == null) {
            log.warn("Producto con id {} no existe", dto.getProductId());
            throw new IllegalArgumentException("El producto con id " + dto.getProductId() + " no existe.");
        }

        OrderDto order = orderClient.getOrderById(dto.getOrderId()).getBody();
        if (order == null) {
            log.warn("Orden con id {} no existe", dto.getOrderId());
            throw new IllegalArgumentException("La orden con id " + dto.getOrderId() + " no existe.");
        }

        Review review = new Review();
        review.setUserId(dto.getUserId());
        review.setProductId(dto.getProductId());
        review.setOrderId(dto.getOrderId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        Review saved = repository.save(review);
        log.info("Review creada con id: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public String delete(Long id) {
        log.info("Eliminando review con id: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Review no encontrada con id: {}", id);
            throw new ReviewNotFoundException("Review no encontrada con id: " + id);
        }
        repository.deleteById(id);
        log.info("Review eliminada con id: {}", id);
        return "Review eliminada exitosamente";
    }

    private ReviewResponseDTO toDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setProductId(review.getProductId());
        dto.setOrderId(review.getOrderId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
