package com.example.ms_discount.controller;

import com.example.ms_discount.dto.response.DiscountResponseDTO;
import com.example.ms_discount.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/discounts/employee")
@RequiredArgsConstructor
public class DiscountControllerEmployee {

    private final DiscountService discountService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<DiscountResponseDTO>> listAll() {
        log.info("Listando todos los cupones");
        return ResponseEntity.ok(discountService.listAll());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<DiscountResponseDTO>> listActive() {
        log.info("Listando cupones activos");
        return ResponseEntity.ok(discountService.listActiveCoupons());
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<DiscountResponseDTO> getByCode(@PathVariable String code) {
        log.info("Obteniendo cupón por código: {}", code);
        return ResponseEntity.ok(discountService.getCouponByCode(code));
    }

    @GetMapping("/exists/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Map<String, Boolean>> couponExists(@PathVariable String code) {
        log.debug("Verificando existencia de cupón por código: {}", code);
        boolean exists = discountService.couponExists(code);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}