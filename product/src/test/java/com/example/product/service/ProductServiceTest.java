package com.example.product.service;

import com.example.product.exception.ProductNotFoundException;
import com.example.product.model.Product;
import com.example.product.model.enums.CareLevel;
import com.example.product.model.enums.Category;
import com.example.product.model.enums.ProductStatus;
import com.example.product.model.enums.Size;
import com.example.product.model.enums.WateringFrequency;
import com.example.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private Product buildProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Monstera deliciosa");
        product.setDescription("Planta tropical de interior");
        product.setPrice(15990.0);
        product.setStock(10);
        product.setCareLevel(CareLevel.LOW);
        product.setCategory(Category.INTERIOR);
        product.setProductStatus(ProductStatus.ACTIVE);
        product.setWateringFrequency(WateringFrequency.WEEKLY);
        product.setSize(Size.MEDIUM);
        return product;
    }

    @Test
    void deberiaRetornarProductoCuandoExiste() {
        // Given
        Product product = buildProduct();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(product));

        // When
        Product resultado = service.getProductById(1L);

        // Then
        assertEquals("Monstera deliciosa", resultado.getName());
        verify(repository).findById(1L);
    }

    @Test
    void deberiaLanzarExcepcionCuandoProductoNoExiste() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ProductNotFoundException.class, () -> service.getProductById(99L));
        verify(repository).findById(99L);
    }

    @Test
    void deberiaRetornarProductoPorNombreCuandoExiste() {
        // Given
        Product product = buildProduct();
        Mockito.when(repository.findByName("Monstera deliciosa")).thenReturn(Optional.of(product));

        // When
        Product resultado = service.getProductByName("Monstera deliciosa");

        // Then
        assertEquals(1L, resultado.getId());
        verify(repository).findByName("Monstera deliciosa");
    }

    @Test
    void deberiaLanzarExcepcionCuandoNombreNoExiste() {
        // Given
        Mockito.when(repository.findByName("Inexistente")).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ProductNotFoundException.class, () -> service.getProductByName("Inexistente"));
        verify(repository).findByName("Inexistente");
    }

    @Test
    void deberiaRetornarTodosLosProductos() {
        // Given
        Mockito.when(repository.findAll()).thenReturn(List.of(buildProduct()));

        // When
        List<Product> resultado = service.getAllProducts();

        // Then
        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void deberiaCrearProducto() {
        // Given
        Product product = buildProduct();
        Mockito.when(repository.save(any(Product.class))).thenReturn(product);

        // When
        Product resultado = service.createProduct(product);

        // Then
        assertEquals(1L, resultado.getId());
        verify(repository).save(product);
    }

    @Test
    void deberiaActualizarProductoCuandoExiste() {
        // Given
        Product existente = buildProduct();
        Product nuevosDatos = buildProduct();
        nuevosDatos.setName("Ficus lyrata");
        nuevosDatos.setPrice(19990.0);

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(repository.save(any(Product.class))).thenReturn(existente);

        // When
        Product resultado = service.updateProduct(1L, nuevosDatos);

        // Then
        assertEquals("Ficus lyrata", resultado.getName());
        assertEquals(19990.0, resultado.getPrice());
        verify(repository).save(existente);
    }

    @Test
    void deberiaEliminarProductoCuandoExiste() {
        // Given
        Product product = buildProduct();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(product));

        // When
        service.deleteProduct(1L);

        // Then
        verify(repository).deleteById(1L);
    }

    @Test
    void noDeberiaEliminarProductoCuandoNoExiste() {
        // Given
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ProductNotFoundException.class, () -> service.deleteProduct(99L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deberiaRetornarProductosPorCategoria() {
        // Given
        Mockito.when(repository.findByCategory(Category.INTERIOR)).thenReturn(List.of(buildProduct()));

        // When
        List<Product> resultado = service.getProductsByCategory("interior");

        // Then
        assertTrue(resultado.size() == 1);
        verify(repository, times(1)).findByCategory(Category.INTERIOR);
    }

    @Test
    void deberiaRetornarProductosPorEstado() {
        // Given
        Mockito.when(repository.findByProductStatus(ProductStatus.ACTIVE)).thenReturn(List.of(buildProduct()));

        // When
        List<Product> resultado = service.getProductsByStatus(ProductStatus.ACTIVE);

        // Then
        assertEquals(1, resultado.size());
        verify(repository).findByProductStatus(ProductStatus.ACTIVE);
    }

    @Test
    void deberiaRetornarProductosConStockMinimo() {
        // Given
        Mockito.when(repository.findByStockGreaterThan(5)).thenReturn(List.of(buildProduct()));

        // When
        List<Product> resultado = service.getProductsWithMinimumStock(5);

        // Then
        assertEquals(1, resultado.size());
        verify(repository).findByStockGreaterThan(5);
    }
}
