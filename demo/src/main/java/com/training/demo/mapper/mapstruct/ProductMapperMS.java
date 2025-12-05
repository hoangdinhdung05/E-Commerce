package com.training.demo.mapper.mapstruct;

import com.training.demo.dto.response.Product.ProductResponse;
import com.training.demo.entity.Product;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Product entity to DTO conversions
 * Automatically generates implementation at compile time
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapperMS {

    /**
     * Map Product entity to ProductResponse DTO
     * @param product Product entity
     * @return ProductResponse DTO
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toProductResponse(Product product);

    /**
     * Map list of Product entities to list of ProductResponse DTOs
     * @param products List of Product entities
     * @return List of ProductResponse DTOs
     */
    List<ProductResponse> toProductResponseList(List<Product> products);

    /**
     * Update existing ProductResponse with Product data
     * @param product Source product entity
     * @param productResponse Target DTO to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    void updateProductResponse(Product product, @MappingTarget ProductResponse productResponse);
}
