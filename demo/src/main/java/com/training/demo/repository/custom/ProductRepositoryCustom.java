package com.training.demo.repository.custom;

import com.training.demo.dto.response.Product.ProductResponse;
import java.util.List;

public interface ProductRepositoryCustom {
    List<ProductResponse> searchByName(String name);
}
