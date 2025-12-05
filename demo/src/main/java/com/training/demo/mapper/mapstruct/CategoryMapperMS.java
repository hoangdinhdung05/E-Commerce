package com.training.demo.mapper.mapstruct;

import com.training.demo.dto.response.Category.CategoryResponse;
import com.training.demo.entity.Category;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Category entity to DTO conversions
 * Automatically generates implementation at compile time
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapperMS {

    /**
     * Map Category entity to CategoryResponse DTO
     * @param category Category entity
     * @return CategoryResponse DTO
     */
    CategoryResponse toCategoryResponse(Category category);

    /**
     * Map list of Category entities to list of CategoryResponse DTOs
     * @param categories List of Category entities
     * @return List of CategoryResponse DTOs
     */
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    /**
     * Update existing CategoryResponse with Category data
     * @param category Source category entity
     * @param categoryResponse Target DTO to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategoryResponse(Category category, @MappingTarget CategoryResponse categoryResponse);
}
