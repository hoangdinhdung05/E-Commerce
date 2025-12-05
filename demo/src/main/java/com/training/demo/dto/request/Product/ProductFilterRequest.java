package com.training.demo.dto.request.Product;

import java.math.BigDecimal;
import java.util.List;

public record ProductFilterRequest(
        String q,
        Long categoryId,
        List<Long> categoryIds,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock
) {}
