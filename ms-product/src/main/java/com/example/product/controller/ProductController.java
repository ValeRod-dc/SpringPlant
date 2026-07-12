package com.example.ms_product.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import com.example.ms_product.dto.request.ProductRequestDto;
import com.example.ms_product.dto.response.ProductResponseDto;
import com.example.ms_product.model.Product;
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
                .map(this::mapToResponseDto)
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
        ProductResponseDto dto = mapToResponseDto(productService.getProductById(id));
        dto.add(linkTo(methodOn(ProductController.class).getProductById(id)).withSelfRel());
        dto.add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("all-products"));
        dto.add(linkTo(methodOn(ProductController.class).updateProduct(id, null)).withRel("update"));
        dto.add(linkTo(methodOn(ProductController.class).deleteProduct(id)).withRel("delete"));
        return ResponseEntity.ok(dto);
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
        ProductResponseDto dto = mapToResponseDto(productService.getProductByName(name));
        dto.add(linkTo(methodOn(ProductController.class).getProductByName(name)).withSelfRel());
        dto.add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("all-products"));
        dto.add(linkTo(methodOn(ProductController.class).updateProduct(dto.getId(), null)).withRel("update"));
        dto.add(linkTo(methodOn(ProductController.class).deleteProduct(dto.getId())).withRel("delete"));
        return ResponseEntity.ok(dto);
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
        ProductResponseDto saved = mapToResponseDto(productService.createProduct(mapToEntity(dto)));
        saved.add(linkTo(methodOn(ProductController.class).getProductById(saved.getId())).withSelfRel());
        saved.add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("all-products"));
        saved.add(linkTo(methodOn(ProductController.class).updateProduct(saved.getId(), null)).withRel("update"));
        saved.add(linkTo(methodOn(ProductController.class).deleteProduct(saved.getId())).withRel("delete"));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

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
        ProductResponseDto updated = mapToResponseDto(productService.updateProduct(id, mapToEntity(dto)));
        updated.add(linkTo(methodOn(ProductController.class).getProductById(id)).withSelfRel());
        updated.add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("all-products"));
        updated.add(linkTo(methodOn(ProductController.class).updateProduct(id, null)).withRel("update"));
        updated.add(linkTo(methodOn(ProductController.class).deleteProduct(id)).withRel("delete"));
        return ResponseEntity.ok(updated);
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
                .map(this::mapToResponseDto)
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
                .map(this::mapToResponseDto)
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
                .map(this::mapToResponseDto)
                .toList());
    }

    private ProductResponseDto mapToResponseDto(Product product) {
        if (product == null) return null;
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .careLevel(product.getCareLevel())
                .size(product.getSize())
                .wateringFrequency(product.getWateringFrequency())
                .productStatus(product.getProductStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private Product mapToEntity(ProductRequestDto dto) {
        Product product = new Product();
        if (dto != null) {
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setStock(dto.getStock());
            product.setCareLevel(dto.getCareLevel());
            product.setCategory(dto.getCategory());
            product.setProductStatus(dto.getProductStatus());
            product.setWateringFrequency(dto.getWateringFrequency());
            product.setSize(dto.getSize());
        }
        return product;
    }
}