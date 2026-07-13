package com.example.ms_product.service;

import com.example.ms_product.exception.ProductNotFoundException;
import com.example.ms_product.model.enums.Category;
import com.example.ms_product.model.Product;
import com.example.ms_product.model.enums.ProductStatus;
import com.example.ms_product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.info("Obteniendo todos los productos");
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        log.info("Buscando producto con id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con id: {}", id);
                    return new ProductNotFoundException("Producto no encontrado con el id: " + id);
                });
    }

    @Transactional(readOnly = true)
    public Product getProductByName(String name) {
        log.info("Buscando producto con nombre: {}", name);
        return productRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con nombre: {}", name);
                    return new ProductNotFoundException("Producto no encontrado con el nombre: " + name);
                });
    }

    @Transactional
    public Product createProduct(Product product) {
        log.info("Creando producto: {}", product.getName());
        validateNameNotTaken(product.getName(), null);
        Product saved = productRepository.save(product);
        log.info("Producto creado con id: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        log.info("Actualizando producto con id: {}", id);
        Product existingProduct = getProductById(id);
        validateNameNotTaken(productDetails.getName(), id);

        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStock(productDetails.getStock());
        existingProduct.setCareLevel(productDetails.getCareLevel());
        existingProduct.setCategory(productDetails.getCategory());
        existingProduct.setProductStatus(productDetails.getProductStatus());
        existingProduct.setWateringFrequency(productDetails.getWateringFrequency());
        existingProduct.setSize(productDetails.getSize());

        Product updated = productRepository.save(existingProduct);
        log.info("Producto actualizado con id: {}", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Eliminando producto con id: {}", id);
        getProductById(id);
        productRepository.deleteById(id);
        log.info("Producto eliminado con id: {}", id);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        log.info("Buscando productos por categoría: {}", category);
        Category cat = Category.valueOf(category.toUpperCase());
        return productRepository.findByCategory(cat);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByStatus(ProductStatus status) {
        log.info("Buscando productos por estado: {}", status);
        return productRepository.findByProductStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsWithMinimumStock(Integer minStock) {
        log.info("Buscando productos con stock mínimo: {}", minStock);
        return productRepository.findByStockGreaterThan(minStock);
    }

    private void validateNameNotTaken(String name, Long excludedId) {
        productRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(excludedId)) {
                log.warn("Ya existe un producto con el nombre: {}", name);
                throw new IllegalArgumentException("Ya existe un producto con el nombre: " + name);
            }
        });
    }
}
