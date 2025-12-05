package com.training.demo.service;

import com.training.demo.dto.request.Cart.AddToCartRequest;
import com.training.demo.dto.request.Cart.UpdateCartItemRequest;
import com.training.demo.dto.response.Cart.CartResponse;

public interface CartService {

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    CartResponse addToCart(Long userId, AddToCartRequest request);

    /**
     * Lấy giỏ hàng của user
     */
    CartResponse getCart(Long userId);

    /**
     * Cập nhật số lượng item trong giỏ
     */
    CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request);

    /**
     * Xóa item khỏi giỏ hàng
     */
    CartResponse removeCartItem(Long userId, Long cartItemId);

    /**
     * Xóa toàn bộ giỏ hàng
     */
    void clearCart(Long userId);

    /**
     * Đếm số lượng items trong giỏ
     */
    int getCartItemCount(Long userId);
}
