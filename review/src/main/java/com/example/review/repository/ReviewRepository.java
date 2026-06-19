package com.example.review.repository;

import com.example.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);
    List<Review> findByOrderId(Long orderId);
}