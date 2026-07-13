package com.example.ms_review.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private Long id;
    private Long userId;
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}