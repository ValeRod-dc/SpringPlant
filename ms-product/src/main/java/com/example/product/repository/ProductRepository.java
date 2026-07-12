package com.example.ms_product.repository;

import com.example.ms_product.model.enums.Category;
import com.example.ms_product.model.Product;
import com.example.ms_product.model.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(Category category);

    List<Product> findByProductStatus(ProductStatus productStatus);

    List<Product> findByStockGreaterThan(Integer minStock);

    Optional<Product> findByName(String name);
}