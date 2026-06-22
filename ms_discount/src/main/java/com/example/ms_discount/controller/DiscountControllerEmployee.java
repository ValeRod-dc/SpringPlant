package com.example.ms_discount.controller;

import com.example.ms_discount.dto.response.DiscountResponseDTO;
import com.example.ms_discount.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/discounts/employee")
@RequiredArgsConstructor
@Tag(name = "Cupones (Empleado)", description = "Endpoints para que empleados y administradores consulten cupones")
public class DiscountControllerEmployee {

    private final DiscountService discountService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Listar todos los cupones", description = "Retorna la lista completa de cupones, activos e inactivos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de cupones")
    })
    public ResponseEntity<List<DiscountResponseDTO>> listAll() {
        log.info("Listando todos los cupones");
        List<DiscountResponseDTO> list = discountService.listAll();
        list.forEach(dto -> dto.add(linkTo(methodOn(DiscountControllerEmployee.class).getByCode(dto.getCode())).withSelfRel()));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Listar cupones activos", description = "Retorna únicamente los cupones que están activos actualmente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de cupones activos")
    })
    public ResponseEntity<List<DiscountResponseDTO>> listActive() {
        log.info("Listando cupones activos");
        List<DiscountResponseDTO> list = discountService.listActiveCoupons();
        list.forEach(dto -> dto.add(linkTo(methodOn(DiscountControllerEmployee.class).getByCode(dto.getCode())).withSelfRel()));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Obtener cupón por código", description = "Retorna el detalle de un cupón a partir de su código.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cupón encontrado"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado")
    })
    public ResponseEntity<DiscountResponseDTO> getByCode(@PathVariable String code) {
        log.info("Obteniendo cupón por código: {}", code);
        DiscountResponseDTO dto = discountService.getCouponByCode(code);
        dto.add(linkTo(methodOn(DiscountControllerEmployee.class).getByCode(code)).withSelfRel());
        dto.add(linkTo(methodOn(DiscountControllerEmployee.class).listAll()).withRel("all"));
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/exists/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Verificar existencia de cupón por código", description = "Indica si existe un cupón con el código dado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    })
    public ResponseEntity<Map<String, Boolean>> couponExists(@PathVariable String code) {
        log.debug("Verificando existencia de cupón por código: {}", code);
        boolean exists = discountService.couponExists(code);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}