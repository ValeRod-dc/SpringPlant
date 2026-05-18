package com.example.ms_shipping.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_shippings")
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shippingId;

    @Column(nullable = false)
    private Long orderId;

    private Long userId;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    private ShippingStatus status;

    private String trackingNumber;

    private LocalDateTime createdAt;

    private LocalDateTime shippedAt;

    private LocalDateTime estimatedDelivery;

    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = ShippingStatus.PENDING;
        if (this.trackingNumber == null) {
            this.trackingNumber = "TRK" + System.currentTimeMillis();
        }
    }
}