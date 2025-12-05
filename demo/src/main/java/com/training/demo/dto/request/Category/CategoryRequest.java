package com.training.demo.dto.request.Category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CategoryRequest {

    @NotBlank(message = "Category name must not be blank")
    private String name;

    private String description;
}
