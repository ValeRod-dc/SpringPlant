package com.example.ms_discount.controller;

import com.example.ms_discount.dto.request.CreateCouponRequest;
import com.example.ms_discount.dto.response.DiscountResponseDTO;
import com.example.ms_discount.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/discounts/admin")
@RequiredArgsConstructor
public class DiscountControllerAdmin {

    private final DiscountService discountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponseDTO> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        log.info("ADMIN - Creando cupón: {}", request.getCode());
        DiscountResponseDTO created = discountService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponseDTO> createCouponForUser(@PathVariable String username,
                                                                   @Valid @RequestBody CreateCouponRequest request) {
        log.info("ADMIN - Creando cupón para usuario: {} con código: {}", username, request.getCode());
        DiscountResponseDTO created = discountService.createCouponForUser(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        log.info("ADMIN - Desactivando cupón con id: {}", id);
        discountService.deactivateCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountResponseDTO> activate(@PathVariable Long id) {
        log.info("ADMIN - Activando cupón con id: {}", id);
        discountService.activateCoupon(id);
        DiscountResponseDTO coupon = discountService.getCouponByCode(
                discountService.findByIdOrThrow(id).getCode());
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Map<String, Boolean>> couponExists(@PathVariable Long id) {
        log.debug("Verificando existencia de cupón por ID: {}", id);
        boolean exists = discountService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}