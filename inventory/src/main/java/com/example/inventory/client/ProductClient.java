package com.example.inventory.client;

import com.example.inventory.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ResponseEntity<ProductDto> getProductById(@PathVariable("id") Long id);
}
