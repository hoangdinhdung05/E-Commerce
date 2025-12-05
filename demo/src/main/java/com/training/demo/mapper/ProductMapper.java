package com.training.demo.mapper;

import com.training.demo.dto.response.Product.ProductResponse;
import com.training.demo.entity.Product;

public class ProductMapper {

    public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .productImageUrl(product.getProductImageUrl())
                .category(CategoryMapper.categoryResponse(product.getCategory()))
                .build();
    }
}
