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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final UserClient userClient;

    public Discount findByIdOrThrow(Long id) {
        log.debug("Buscando cupón por ID: {}", id);
        return discountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cupón no encontrado - ID: {}", id);
                    return new CouponNotFoundException("Cupón no encontrado con ID: " + id);
                });
    }

    public Discount findByCodeOrThrow(String code) {
        log.debug("Buscando cupón por código: {}", code);
        return discountRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("Cupón no encontrado - Código: {}", code);
                    return new CouponNotFoundException("Cupón no encontrado con código: " + code);
                });
    }

    // Crear cupón general (sin usuario específico)
    @Transactional
    public DiscountResponseDTO createCoupon(CreateCouponRequest request) {
        log.info("Creando cupón con código: {}", request.getCode());

        // Verificar si ya existe
        if (discountRepository.findByCode(request.getCode().toUpperCase()).isPresent()) {
            throw new CouponNotFoundException("Ya existe un cupón con el código: " + request.getCode());
        }

        Discount discount = new Discount();
        discount.setCode(request.getCode().toUpperCase());
        discount.setDescription(request.getDescription());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setValidFrom(request.getValidFrom());
        discount.setValidUntil(request.getValidUntil());
        discount.setMaxUses(request.getMaxUses() == 0 ? null : request.getMaxUses());
        discount.setCurrentUses(0);
        discount.setMinPurchaseAmount(request.getMinPurchaseAmount());
        discount.setActive(request.getActive() != null ? request.getActive() : true);
        discount.setApplicableProductIds(request.getApplicableProductIds());

        Discount saved = discountRepository.save(discount);
        log.info("Cupón creado con id: {}", saved.getDiscountId());
        return mapToResponseDTO(saved);
    }

    // Crear cupón para un usuario específico (solo ADMIN)
    @Transactional
    public DiscountResponseDTO createCouponForUser(String username, CreateCouponRequest request) {
        log.info("Creando cupón para usuario: {} con código: {}", username, request.getCode());

        // Verificar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe al crear cupón: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // Verificar si ya existe
        if (discountRepository.findByCode(request.getCode().toUpperCase()).isPresent()) {
            throw new CouponNotFoundException("Ya existe un cupón con el código: " + request.getCode());
        }

        Discount discount = new Discount();
        discount.setCode(request.getCode().toUpperCase() + "_" + username);
        discount.setDescription(request.getDescription() + " (Usuario: " + username + ")");
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setValidFrom(request.getValidFrom());
        discount.setValidUntil(request.getValidUntil());
        discount.setMaxUses(1); // Cupón de un solo uso por usuario
        discount.setCurrentUses(0);
        discount.setMinPurchaseAmount(request.getMinPurchaseAmount());
        discount.setActive(request.getActive() != null ? request.getActive() : true);
        discount.setApplicableProductIds(request.getApplicableProductIds());

        Discount saved = discountRepository.save(discount);
        log.info("Cupón creado para usuario {} con id: {}", username, saved.getDiscountId());
        return mapToResponseDTO(saved);
    }

    public List<DiscountResponseDTO> listAll() {
        log.debug("Listando todos los cupones");
        return discountRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<DiscountResponseDTO> listActiveCoupons() {
        log.debug("Listando cupones activos");
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findByActiveTrueAndValidFromBeforeAndValidUntilAfter(now, now).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public DiscountResponseDTO getCouponByCode(String code) {
        log.debug("Buscando cupón por código: {}", code);
        Discount discount = findByCodeOrThrow(code);
        return mapToResponseDTO(discount);
    }

    public boolean couponExists(String code) {
        log.debug("Verificando existencia de cupón: {}", code);
        boolean exists = discountRepository.findByCode(code.toUpperCase()).isPresent();
        log.debug("Cupón {} - Existe: {}", code, exists);
        return exists;
    }

    @Transactional
    public DiscountResult validateCoupon(ValidateCouponRequest request) {
        String code = request.getCode().toUpperCase();
        log.info("Validando cupón: {} con total carrito: {}", code, request.getCartTotal());

        Discount coupon = discountRepository.findByCode(code).orElse(null);

        if (coupon == null) {
            log.warn("Cupón inválido: {} no existe", code);
            return new DiscountResult(false, 0.0, "Cupón no válido", code);
        }

        if (!coupon.getActive()) {
            log.warn("Cupón {} desactivado", code);
            return new DiscountResult(false, 0.0, "Cupón desactivado", code);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            log.warn("Cupón {} fuera de fecha vigencia", code);
            return new DiscountResult(false, 0.0, "Cupón fuera de fecha de vigencia", code);
        }

        if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses()) {
            log.warn("Cupón {} ha alcanzado su límite de usos", code);
            return new DiscountResult(false, 0.0, "Cupón ya alcanzó su límite de usos", code);
        }

        if (request.getCartTotal() < coupon.getMinPurchaseAmount()) {
            log.debug("Monto mínimo no alcanzado para cupón {}, total: {}", code, request.getCartTotal());
            return new DiscountResult(false, 0.0,
                    "Monto mínimo de compra: $" + coupon.getMinPurchaseAmount(), code);
        }

        Double discountAmount = 0.0;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = request.getCartTotal() * (coupon.getDiscountValue() / 100.0);
        } else {
            discountAmount = Math.min(coupon.getDiscountValue(), request.getCartTotal());
        }
        discountAmount = Math.round(discountAmount * 100.0) / 100.0;

        log.info("Cupón {} aplicado correctamente, descuento: ${}", code, discountAmount);
        return new DiscountResult(true, discountAmount, "Cupón aplicado correctamente", code);
    }

    @Transactional
    public DiscountResult useCoupon(String code, Double cartTotal) {
        log.info("Usando cupón: {} con total carrito: ${}", code, cartTotal);

        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode(code);
        request.setCartTotal(cartTotal);

        DiscountResult validation = validateCoupon(request);
        if (!validation.isValid()) {
            return validation;
        }

        Discount coupon = findByCodeOrThrow(code);
        coupon.setCurrentUses(coupon.getCurrentUses() + 1);
        discountRepository.save(coupon);

        log.info("Cupón {} usado, usos actuales: {}/{}", code, coupon.getCurrentUses(),
                coupon.getMaxUses() != null ? coupon.getMaxUses() : "∞");
        return validation;
    }

    @Transactional
    public void deactivateCoupon(Long id) {
        log.info("Desactivando cupón con id: {}", id);
        Discount coupon = findByIdOrThrow(id);
        coupon.setActive(false);
        discountRepository.save(coupon);
        log.info("Cupón {} desactivado", coupon.getCode());
    }

    @Transactional
    public void activateCoupon(Long id) {
        log.info("Activando cupón con id: {}", id);
        Discount coupon = findByIdOrThrow(id);
        coupon.setActive(true);
        discountRepository.save(coupon);
        log.info("Cupón {} activado", coupon.getCode());
    }

    public boolean existsById(Long id) {
        log.debug("Verificando existencia de cupón por ID: {}", id);
        boolean exists = discountRepository.existsById(id);
        log.debug("Cupón ID: {} - Existe: {}", id, exists);
        return exists;
    }

    private DiscountResponseDTO mapToResponseDTO(Discount discount) {
        return DiscountResponseDTO.builder()
                .discountId(discount.getDiscountId())
                .code(discount.getCode())
                .description(discount.getDescription())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .validFrom(discount.getValidFrom())
                .validUntil(discount.getValidUntil())
                .maxUses(discount.getMaxUses())
                .currentUses(discount.getCurrentUses())
                .minPurchaseAmount(discount.getMinPurchaseAmount())
                .active(discount.getActive())
                .applicableProductIds(discount.getApplicableProductIds())
                .build();
    }
}