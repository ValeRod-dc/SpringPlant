package com.example.ms_discount.service;

import com.example.ms_discount.client.UserClient;
import com.example.ms_discount.dto.request.CreateCouponRequest;
import com.example.ms_discount.dto.request.ValidateCouponRequest;
import com.example.ms_discount.dto.response.DiscountResponseDTO;
import com.example.ms_discount.dto.response.DiscountResult;
import com.example.ms_discount.exception.custom.*;
import com.example.ms_discount.model.Discount;
import com.example.ms_discount.model.DiscountType;
import com.example.ms_discount.repository.DiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private DiscountService discountService;

    private CreateCouponRequest createRequest;
    private Discount savedDiscount;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        createRequest = new CreateCouponRequest();
        createRequest.setCode("TEST10");
        createRequest.setDescription("Test coupon 10%");
        createRequest.setDiscountType(DiscountType.PERCENTAGE);
        createRequest.setDiscountValue(10.0);
        createRequest.setValidFrom(now.minusDays(1));
        createRequest.setValidUntil(now.plusDays(30));
        createRequest.setMaxUses(5);
        createRequest.setMinPurchaseAmount(100.0);
        createRequest.setActive(true);

        savedDiscount = new Discount();
        savedDiscount.setDiscountId(1L);
        savedDiscount.setCode("TEST10");
        savedDiscount.setDescription("Test coupon 10%");
        savedDiscount.setDiscountType(DiscountType.PERCENTAGE);
        savedDiscount.setDiscountValue(10.0);
        savedDiscount.setValidFrom(now.minusDays(1));
        savedDiscount.setValidUntil(now.plusDays(30));
        savedDiscount.setMaxUses(5);
        savedDiscount.setCurrentUses(0);
        savedDiscount.setMinPurchaseAmount(100.0);
        savedDiscount.setActive(true);
    }

    // ========== CREATE COUPON ==========

    @Test
    void shouldCreateCouponWhenValidRequest() {
        // Given
        when(discountRepository.findByCode("TEST10")).thenReturn(Optional.empty());
        when(discountRepository.save(any(Discount.class))).thenReturn(savedDiscount);

        // When
        DiscountResponseDTO result = discountService.createCoupon(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getDiscountId());
        assertEquals("TEST10", result.getCode());
        assertEquals(10.0, result.getDiscountValue());
        assertTrue(result.getActive());

        verify(discountRepository).findByCode("TEST10");
        verify(discountRepository).save(any(Discount.class));
    }

    @Test
    void shouldThrowCouponNotFoundExceptionWhenCouponCodeAlreadyExists() {
        // Given
        when(discountRepository.findByCode("TEST10")).thenReturn(Optional.of(savedDiscount));

        // When & Then
        assertThrows(CouponNotFoundException.class, () ->
                discountService.createCoupon(createRequest)
        );

        verify(discountRepository).findByCode("TEST10");
        verify(discountRepository, never()).save(any(Discount.class));
    }

    @Test
    void shouldCreateCouponForUserWhenUserExists() {
        // Given
        String username = "testUser";
        when(userClient.userExists(username)).thenReturn(true);
        when(discountRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(discountRepository.save(any(Discount.class))).thenReturn(savedDiscount);

        // When
        DiscountResponseDTO result = discountService.createCouponForUser(username, createRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.getCode().contains("TEST10"));
        assertTrue(result.getDescription().contains(username));

        verify(userClient).userExists(username);
        verify(discountRepository).save(any(Discount.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistForCoupon() {
        // Given
        String username = "nonExistingUser";
        when(userClient.userExists(username)).thenReturn(false);

        // When & Then
        assertThrows(UserNotFoundException.class, () ->
                discountService.createCouponForUser(username, createRequest)
        );

        verify(userClient).userExists(username);
        verify(discountRepository, never()).save(any(Discount.class));
    }

    // ========== VALIDATE COUPON ==========

    @Test
    void shouldValidateCouponWhenValid() {
        // Given
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("TEST10");
        request.setCartTotal(150.0);

        when(discountRepository.findByCode("TEST10")).thenReturn(Optional.of(savedDiscount));

        // When
        DiscountResult result = discountService.validateCoupon(request);

        // Then
        assertTrue(result.isValid());
        assertEquals(15.0, result.getDiscountAmount()); // 10% de 150 = 15
        assertEquals("Cupón aplicado correctamente", result.getMessage());
        assertEquals("TEST10", result.getCouponCode());

        verify(discountRepository).findByCode("TEST10");
    }

    @Test
    void shouldReturnInvalidWhenCouponNotFound() {
        // Given
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("INVALID");
        request.setCartTotal(150.0);

        when(discountRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // When
        DiscountResult result = discountService.validateCoupon(request);

        // Then
        assertFalse(result.isValid());
        assertEquals(0.0, result.getDiscountAmount());
        assertEquals("Cupón no válido", result.getMessage());

        verify(discountRepository).findByCode("INVALID");
    }

    @Test
    void shouldReturnInvalidWhenCouponInactive() {
        // Given
        savedDiscount.setActive(false);
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("TEST10");
        request.setCartTotal(150.0);

        when(discountRepository.findByCode("TEST10")).thenReturn(Optional.of(savedDiscount));

        // When
        DiscountResult result = discountService.validateCoupon(request);

        // Then
        assertFalse(result.isValid());
        assertEquals("Cupón desactivado", result.getMessage());
    }

    @Test
    void shouldReturnInvalidWhenCouponExpired() {
        // Given
        savedDiscount.setValidFrom(now.minusDays(60));
        savedDiscount.setValidUntil(now.minusDays(1)); // Expirado
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("TEST10");
        request.setCartTotal(150.0);

        when(discountRepository.findByCode("TEST10")).thenReturn(Optional.of(savedDiscount));

        // When
        DiscountResult result = discountService.validateCoupon(request);

        // Then
        assertFalse(result.isValid());
        assertEquals("Cupón fuera de fecha de vigencia", result.getMessage());
    }

    @Test
    void shouldReturnInvalidWhenMinPurchaseNotMet() {
        // Given
        savedDiscount.setMinPurchaseAmount(200.0);
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("TEST10");
        request.setCartTotal(150.0);

        when(discountRepository.findByCode("TEST10")).thenReturn(Optional.of(savedDiscount));

        // When
        DiscountResult result = discountService.validateCoupon(request);

        // Then
        assertFalse(result.isValid());
        assertEquals("Monto mínimo de compra: $200.0", result.getMessage());
    }

    @Test
    void shouldReturnInvalidWhenMaxUsesReached() {
        // Given
        savedDiscount.setMaxUses(1);
        savedDiscount.setCurrentUses(1);
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("TEST10");
        request.setCartTotal(150.0);

        when(discountRepository.findByCode("TEST10")).thenReturn(Optional.of(savedDiscount));

        // When
        DiscountResult result = discountService.validateCoupon(request);

        // Then
        assertFalse(result.isValid());
        assertEquals("Cupón ya alcanzó su límite de usos", result.getMessage());
    }

    // ========== USE COUPON ==========

    @Test
    void shouldUseCouponAndIncrementUses() {
        // Given
        String code = "TEST10";
        Double cartTotal = 150.0;
        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode(code);
        request.setCartTotal(cartTotal);

        when(discountRepository.findByCode(code)).thenReturn(Optional.of(savedDiscount));
        when(discountRepository.save(any(Discount.class))).thenReturn(savedDiscount);

        // When
        DiscountResult result = discountService.useCoupon(code, cartTotal);

        // Then
        assertTrue(result.isValid());
        assertEquals(1, savedDiscount.getCurrentUses());

        verify(discountRepository).findByCode(code);
        verify(discountRepository).save(savedDiscount);
    }

    // ========== DEACTIVATE / ACTIVATE ==========

    @Test
    void shouldDeactivateCoupon() {
        // Given
        Long discountId = 1L;
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(savedDiscount));

        // When
        discountService.deactivateCoupon(discountId);

        // Then
        assertFalse(savedDiscount.getActive());
        verify(discountRepository).save(savedDiscount);
    }

    @Test
    void shouldActivateCoupon() {
        // Given
        Long discountId = 1L;
        savedDiscount.setActive(false);
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(savedDiscount));

        // When
        discountService.activateCoupon(discountId);

        // Then
        assertTrue(savedDiscount.getActive());
        verify(discountRepository).save(savedDiscount);
    }

    @Test
    void shouldThrowCouponNotFoundExceptionWhenDeactivateNotFound() {
        // Given
        Long discountId = 999L;
        when(discountRepository.findById(discountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CouponNotFoundException.class, () ->
                discountService.deactivateCoupon(discountId)
        );

        verify(discountRepository, never()).save(any(Discount.class));
    }
}