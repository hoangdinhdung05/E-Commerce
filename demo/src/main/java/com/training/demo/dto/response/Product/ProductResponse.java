package com.training.demo.dto.response.Product;

import com.training.demo.dto.response.Category.CategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
    private String productImageUrl;
    private CategoryResponse category;

    // Constructor cho JPQL projection
    public ProductResponse(Long id, String name, String description,
                           BigDecimal price, Integer quantity, String productImageUrl,
                           Long categoryId, String categoryName, String categoryDescription) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity != null ? quantity : 0;
        this.productImageUrl = productImageUrl;

        // Tạo nested CategoryResponse
        if (categoryId != null) {
            this.category = CategoryResponse.builder()
                    .id(categoryId)
                    .name(categoryName)
                    .description(categoryDescription)
                    .build();
        }
    }
}