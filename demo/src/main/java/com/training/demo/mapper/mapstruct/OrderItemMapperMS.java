package com.training.demo.mapper.mapstruct;

import com.training.demo.dto.response.Order.OrderItemResponse;
import com.training.demo.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for OrderItem entity to DTO conversions
 * Automatically generates implementation at compile time
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderItemMapperMS {

    /**
     * Map OrderItem entity to OrderItemResponse DTO
     * @param orderItem OrderItem entity
     * @return OrderItemResponse DTO
     */
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.productImageUrl")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    /**
     * Map list of OrderItem entities to list of OrderItemResponse DTOs
     * @param orderItems List of OrderItem entities
     * @return List of OrderItemResponse DTOs
     */
    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);
}
