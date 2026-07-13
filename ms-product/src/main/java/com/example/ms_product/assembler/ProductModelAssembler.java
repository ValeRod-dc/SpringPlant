package com.example.ms_product.assembler;

import com.example.ms_product.controller.ProductController;
import com.example.ms_product.dto.response.ProductResponseDto;
import com.example.ms_product.mapper.ProductMapper;
import com.example.ms_product.model.Product;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ProductModelAssembler implements RepresentationModelAssembler<Product, ProductResponseDto> {

    private final ProductMapper mapper;

    public ProductModelAssembler(ProductMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ProductResponseDto toModel(Product product) {
        ProductResponseDto dto = mapper.toResponseDto(product);
        dto.add(linkTo(methodOn(ProductController.class).getProductById(product.getId())).withSelfRel());
        dto.add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("all-products"));
        dto.add(linkTo(methodOn(ProductController.class).updateProduct(product.getId(), null)).withRel("update"));
        dto.add(linkTo(methodOn(ProductController.class).deleteProduct(product.getId())).withRel("delete"));
        return dto;
    }
}
