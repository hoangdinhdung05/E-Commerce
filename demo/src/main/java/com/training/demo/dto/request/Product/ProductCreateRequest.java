package com.training.demo.dto.request.Product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductCreateRequest {
    @NotBlank(message = "Product name must not be blank")
    private String name;

    private String description;

    @NotNull(message = "Price must not be null")
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer quantity;

    @NotNull(message = "Category ID must not be null")
    private Long categoryId;

    @NotBlank(message = "Product image URL must not be blank")
    private String imageUrl; // FE truyền từ Upload API
}
