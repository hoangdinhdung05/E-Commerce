package com.training.demo.mapper;

import com.training.demo.dto.response.Cart.CartItemResponse;
import com.training.demo.dto.response.Cart.CartResponse;
import com.training.demo.entity.Cart;
import com.training.demo.entity.CartItem;
import java.util.stream.Collectors;

public class CartMapper {

    public static CartResponse toCartResponse(Cart cart) {
        if (cart == null) return null;

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .items(cart.getCartItems() != null
                        ? cart.getCartItems().stream()
                            .map(CartMapper::toCartItemResponse)
                            .collect(Collectors.toList())
                        : null)
                .totalItems(cart.getTotalItems())
                .totalAmount(cart.getTotalAmount())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    public static CartItemResponse toCartItemResponse(CartItem item) {
        if (item == null) return null;

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .price(item.getPriceAtAdd())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .productImageUrl(item.getProduct() != null ? item.getProduct().getProductImageUrl() : null)
                .availableStock(item.getProduct() != null ? item.getProduct().getQuantity() : 0)
                .build();
    }
}
