package com.training.demo.dto.request.Product;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class ProductRequest {

    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be >= 0")
    private BigDecimal price;

    @Min(value = 0, message = "Quantity must be >= 0")
    private Integer quantity;

    private Long categoryId;

    private String imageUrl; // URL từ Upload API
}
