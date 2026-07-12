package com.example.ms_shipping.repository;

import com.example.ms_shipping.model.Shipping;
import com.example.ms_shipping.model.ShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long> {
    List<Shipping> findByOrderId(Long orderId);
    List<Shipping> findByUserId(Long userId);
    List<Shipping> findByStatus(ShippingStatus status);
    Optional<Shipping> findByOrderIdAndStatusNot(Long orderId, ShippingStatus status);
    boolean existsByOrderId(Long orderId);
}