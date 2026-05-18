package com.example.ms_discount.controller;

import com.example.ms_discount.dto.response.DiscountResult;
import com.example.ms_discount.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping("/use")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<DiscountResult> useCoupon(@RequestParam String code,
                                                    @RequestParam Double cartTotal) {
        log.info("Usando cupón: {} con total carrito: ${}", code, cartTotal);
        DiscountResult result = discountService.useCoupon(code, cartTotal);
        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/validate/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<DiscountResult> validateCoupon(@PathVariable String code,
                                                         @RequestParam(required = false) Double cartTotal) {
        log.info("Validando cupón: {} con total carrito: ${}", code, cartTotal != null ? cartTotal : 0);

        com.example.ms_discount.dto.request.ValidateCouponRequest request =
                new com.example.ms_discount.dto.request.ValidateCouponRequest();
        request.setCode(code);
        request.setCartTotal(cartTotal != null ? cartTotal : 0.0);

        DiscountResult result = discountService.validateCoupon(request);
        return ResponseEntity.ok(result);
    }
}