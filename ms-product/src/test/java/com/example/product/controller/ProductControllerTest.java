package com.example.ms_product.controller;

import com.example.ms_product.exception.ProductNotFoundException;
import com.example.ms_product.model.Product;
import com.example.ms_product.model.enums.CareLevel;
import com.example.ms_product.model.enums.Category;
import com.example.ms_product.model.enums.ProductStatus;
import com.example.ms_product.model.enums.Size;
import com.example.ms_product.model.enums.WateringFrequency;
import com.example.ms_product.security.jwt.JwtService;
import com.example.ms_product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @MockitoBean
    private JwtService jwtService;

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
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarProductoPorId() throws Exception {
        // Given
        when(service.getProductById(1L)).thenReturn(buildProduct());

        // When / Then
        mockMvc.perform(get("/api/v1/products/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monstera deliciosa"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.all-products.href").exists());

        verify(service).getProductById(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornar404CuandoProductoNoExiste() throws Exception {
        // Given
        when(service.getProductById(99L)).thenThrow(new ProductNotFoundException("Producto no encontrado con el id: 99"));

        // When / Then
        mockMvc.perform(get("/api/v1/products/99")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound());

        verify(service).getProductById(99L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarTodosLosProductos() throws Exception {
        // Given
        when(service.getAllProducts()).thenReturn(List.of(buildProduct()));

        // When / Then
        mockMvc.perform(get("/api/v1/products")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Monstera deliciosa"));

        verify(service).getAllProducts();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaCrearProducto() throws Exception {
        // Given
        when(service.createProduct(any(Product.class))).thenReturn(buildProduct());

        String json = """
                {
                    "name": "Monstera deliciosa",
                    "description": "Planta tropical de interior",
                    "price": 15990.0,
                    "stock": 10,
                    "careLevel": "LOW",
                    "category": "INTERIOR",
                    "productStatus": "ACTIVE",
                    "wateringFrequency": "WEEKLY",
                    "size": "MEDIUM"
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType("application/json")
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monstera deliciosa"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.delete.href").exists());

        verify(service).createProduct(any(Product.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornar400CuandoFaltanCamposObligatorios() throws Exception {
        // Given
        String json = """
                {
                    "name": "Monstera deliciosa"
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaEliminarProducto() throws Exception {
        // When / Then
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteProduct(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarProductoPorNombre() throws Exception {
        // Given
        when(service.getProductByName("Monstera")).thenReturn(buildProduct());

        // When / Then
        mockMvc.perform(get("/api/v1/products/name/Monstera")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monstera deliciosa"))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(service).getProductByName("Monstera");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaActualizarProducto() throws Exception {
        // Given
        Product actualizado = buildProduct();
        actualizado.setName("Ficus lyrata");
        when(service.updateProduct(eq(1L), any(Product.class))).thenReturn(actualizado);

        String json = """
                {
                    "name": "Ficus lyrata",
                    "description": "Planta tropical de interior",
                    "price": 19990.0,
                    "stock": 5,
                    "careLevel": "LOW",
                    "category": "INTERIOR",
                    "productStatus": "ACTIVE",
                    "wateringFrequency": "WEEKLY",
                    "size": "MEDIUM"
                }
                """;

        // When / Then
        mockMvc.perform(put("/api/v1/products/1")
                        .contentType("application/json")
                        .accept(MediaTypes.HAL_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ficus lyrata"))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(service).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarProductosPorCategoria() throws Exception {
        // Given
        when(service.getProductsByCategory("interior")).thenReturn(List.of(buildProduct()));

        // When / Then
        mockMvc.perform(get("/api/v1/products/category/interior")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("INTERIOR"));

        verify(service).getProductsByCategory("interior");
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deberiaRetornarProductosPorEstado() throws Exception {
        // Given
        when(service.getProductsByStatus(ProductStatus.ACTIVE)).thenReturn(List.of(buildProduct()));

        // When / Then
        mockMvc.perform(get("/api/v1/products/status/ACTIVE")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productStatus").value("ACTIVE"));

        verify(service).getProductsByStatus(ProductStatus.ACTIVE);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deberiaRetornarProductosConStockMinimo() throws Exception {
        // Given
        when(service.getProductsWithMinimumStock(5)).thenReturn(List.of(buildProduct()));

        // When / Then
        mockMvc.perform(get("/api/v1/products/stock/5")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(10));

        verify(service).getProductsWithMinimumStock(5);
    }
}
