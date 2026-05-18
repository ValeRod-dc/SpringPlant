package com.example.ms_discount.repository;

import com.example.ms_discount.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByCode(String code);

    List<Discount> findByActiveTrueAndValidFromBeforeAndValidUntilAfter(LocalDateTime now, LocalDateTime now2);
}
