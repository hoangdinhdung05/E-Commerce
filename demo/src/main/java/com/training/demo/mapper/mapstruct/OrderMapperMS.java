package com.training.demo.mapper.mapstruct;

import com.training.demo.dto.response.Order.OrderResponse;
import com.training.demo.entity.Order;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Order entity to DTO conversions
 * Automatically generates implementation at compile time
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrderItemMapperMS.class}
)
public interface OrderMapperMS {

    /**
     * Map Order entity to OrderResponse DTO
     * @param order Order entity
     * @return OrderResponse DTO
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "orderItems", source = "orderItems")
    OrderResponse toOrderResponse(Order order);

    /**
     * Map list of Order entities to list of OrderResponse DTOs
     * @param orders List of Order entities
     * @return List of OrderResponse DTOs
     */
    List<OrderResponse> toOrderResponseList(List<Order> orders);
}
