package com.training.demo.mapper;

import com.training.demo.dto.response.Order.OrderItemResponse;
import com.training.demo.dto.response.Order.OrderResponse;
import com.training.demo.entity.Order;
import com.training.demo.entity.OrderItem;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .username(order.getUser() != null ? order.getUser().getUsername() : null)
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .phoneNumber(order.getPhoneNumber())
                .receiverName(order.getReceiverName())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .note(order.getNote())
                .items(order.getOrderItems() != null 
                    ? order.getOrderItems().stream()
                        .map(OrderMapper::toOrderItemResponse)
                        .collect(Collectors.toList())
                    : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem item) {
        if (item == null) return null;

        // If productImageUrl is null in OrderItem, try to get it from Product
        String imageUrl = item.getProductImageUrl();
        if (imageUrl == null && item.getProduct() != null) {
            imageUrl = item.getProduct().getProductImageUrl();
        }

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .productImageUrl(imageUrl)
                .build();
    }
}
