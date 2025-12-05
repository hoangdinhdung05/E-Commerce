package com.training.demo.controller;

import com.training.demo.dto.request.Cart.AddToCartRequest;
import com.training.demo.dto.request.Cart.UpdateCartItemRequest;
import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.security.SecurityUtils;
import com.training.demo.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Slf4j
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Lấy giỏ hàng của user hiện tại
     */
    @GetMapping
    public ResponseEntity<?> getCart() {
        log.info("[CartController] Getting cart");
        Long userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(BaseResponse.success(
                cartService.getCart(userId)
        ));
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request) {
        log.info("[CartController] Adding product {} to cart", request.getProductId());
        Long userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        cartService.addToCart(userId, request)
                ));
    }

    /**
     * Cập nhật số lượng item trong giỏ
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("[CartController] Updating cart item {}", cartItemId);
        Long userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(BaseResponse.success(
                cartService.updateCartItem(userId, cartItemId, request)
        ));
    }

    /**
     * Xóa item khỏi giỏ hàng
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long cartItemId) {
        log.info("[CartController] Removing cart item {}", cartItemId);
        Long userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(BaseResponse.success(
                cartService.removeCartItem(userId, cartItemId)
        ));
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        log.info("[CartController] Clearing cart");
        Long userId = SecurityUtils.getCurrentUserId();

        cartService.clearCart(userId);

        return ResponseEntity.ok(BaseResponse.success(
                "Cart cleared successfully"
        ));
    }

    /**
     * Đếm số lượng items trong giỏ
     */
    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount() {
        log.info("[CartController] Getting cart item count");
        Long userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(BaseResponse.success(
                cartService.getCartItemCount(userId)
        ));
    }
}
