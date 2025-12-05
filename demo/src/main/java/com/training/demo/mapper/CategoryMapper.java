package com.training.demo.mapper;

import com.training.demo.dto.response.Category.CategoryResponse;
import com.training.demo.entity.Category;

public class CategoryMapper {

    public static CategoryResponse categoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
