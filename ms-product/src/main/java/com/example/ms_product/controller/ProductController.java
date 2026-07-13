package com.example.ms_product.controller;

import com.example.ms_product.assembler.ProductModelAssembler;
import com.example.ms_product.dto.request.ProductRequestDto;
import com.example.ms_product.dto.response.ProductResponseDto;
import com.example.ms_product.mapper.ProductMapper;
import com.example.ms_product.model.enums.ProductStatus;
import com.example.ms_product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper = new ProductMapper();
    private final ProductModelAssembler productAssembler = new ProductModelAssembler(productMapper);

    @Operation(
            summary = "Obtener todos los productos",
            description = "Retorna la lista completa de productos disponibles. Accesible para ADMIN, EMPLOYEE y CLIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts()
                .stream()
                .map(productAssembler::toModel)
                .toList());
    }

    @Operation(
            summary = "Obtener producto por ID",
            description = "Busca y retorna un producto según su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productAssembler.toModel(productService.getProductById(id)));
    }

    @Operation(
            summary = "Obtener producto por nombre",
            description = "Busca y retorna un producto según su nombre exacto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<ProductResponseDto> getProductByName(@PathVariable String name) {
        return ResponseEntity.ok(productAssembler.toModel(productService.getProductByName(name)));
    }

    @Operation(
            summary = "Crear producto",
            description = "Registra un nuevo producto en el sistema. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto dto) {
        var saved = productService.createProduct(productMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(productAssembler.toModel(saved));
    }

    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza los datos de un producto existente. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDto dto) {
        var updated = productService.updateProduct(id, productMapper.toEntity(dto));
        return ResponseEntity.ok(productAssembler.toModel(updated));
    }

    @Operation(
            summary = "Eliminar producto",
            description = "Elimina un producto del sistema por su ID. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Obtener productos por categoría",
            description = "Retorna todos los productos que pertenecen a una categoría específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Categoría inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category)
                .stream()
                .map(productAssembler::toModel)
                .toList());
    }

    @Operation(
            summary = "Obtener productos por estado",
            description = "Retorna todos los productos que tienen un estado específico (ej: AVAILABLE, OUT_OF_STOCK)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public ResponseEntity<List<ProductResponseDto>> getProductsByStatus(@PathVariable ProductStatus status) {
        return ResponseEntity.ok(productService.getProductsByStatus(status)
                .stream()
                .map(productAssembler::toModel)
                .toList());
    }

    @Operation(
            summary = "Obtener productos con stock mínimo",
            description = "Retorna productos cuyo stock es mayor o igual al valor indicado. Solo accesible para ADMIN y EMPLOYEE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Valor de stock inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/stock/{minStock}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<ProductResponseDto>> getProductsWithMinimumStock(@PathVariable Integer minStock) {
        return ResponseEntity.ok(productService.getProductsWithMinimumStock(minStock)
                .stream()
                .map(productAssembler::toModel)
                .toList());
    }
}
