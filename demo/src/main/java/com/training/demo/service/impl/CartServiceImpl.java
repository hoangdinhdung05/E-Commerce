package com.training.demo.service.impl;

import com.training.demo.dto.request.Cart.AddToCartRequest;
import com.training.demo.dto.request.Cart.UpdateCartItemRequest;
import com.training.demo.dto.response.Cart.CartResponse;
import com.training.demo.entity.Cart;
import com.training.demo.entity.CartItem;
import com.training.demo.entity.Product;
import com.training.demo.entity.User;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.mapper.CartMapper;
import com.training.demo.repository.CartItemRepository;
import com.training.demo.repository.CartRepository;
import com.training.demo.repository.ProductRepository;
import com.training.demo.repository.UserRepository;
import com.training.demo.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        log.info("[CartService] Adding product {} to cart for user {}", request.getProductId(), userId);

        // Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + request.getProductId()));

        // Check stock
        if (product.getQuantity() < request.getQuantity()) {
            throw new BadRequestException("Product '" + product.getName() + "' is out of stock. Available: " + product.getQuantity());
        }

        // Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            // Validate stock for new quantity
            if (product.getQuantity() < newQuantity) {
                throw new BadRequestException("Cannot add more. Available stock: " + product.getQuantity() + ", current in cart: " + item.getQuantity());
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            log.info("[CartService] Updated cart item quantity: {} -> {}", item.getQuantity() - request.getQuantity(), newQuantity);
        } else {
            // Create new cart item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .priceAtAdd(product.getPrice())
                    .build();

            cart.addCartItem(newItem);
            cartItemRepository.save(newItem);
            log.info("[CartService] Added new item to cart: product={}, quantity={}", product.getName(), request.getQuantity());
        }

        cart = cartRepository.save(cart);

        // Reload cart with items
        cart = cartRepository.findByUserIdWithItemsAndProducts(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        return CartMapper.toCartResponse(cart);
    }

    /**
     * Lấy giỏ hàng của user
     */
    @Override
    public CartResponse getCart(Long userId) {
        log.info("[CartService] Getting cart for user {}", userId);

        Cart cart = cartRepository.findByUserIdWithItemsAndProducts(userId)
                .orElse(null);

        if (cart == null) {
            // Return empty cart
            return CartResponse.builder()
                    .userId(userId)
                    .items(java.util.Collections.emptyList())
                    .totalItems(0)
                    .totalAmount(java.math.BigDecimal.ZERO)
                    .build();
        }

        return CartMapper.toCartResponse(cart);
    }

    /**
     * Cập nhật số lượng item trong giỏ
     */
    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("[CartService] Updating cart item {} for user {}", cartItemId, userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found with id: " + cartItemId));

        // Validate ownership
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to update this cart item");
        }

        // Validate stock
        Product product = cartItem.getProduct();
        if (product.getQuantity() < request.getQuantity()) {
            throw new BadRequestException("Cannot update quantity. Available stock: " + product.getQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        log.info("[CartService] Cart item updated: quantity={}", request.getQuantity());

        // Reload cart
        Cart cart = cartRepository.findByUserIdWithItemsAndProducts(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        return CartMapper.toCartResponse(cart);
    }

    /**
     * Xóa item khỏi giỏ hàng
     */
    @Override
    @Transactional
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        log.info("[CartService] Removing cart item {} for user {}", cartItemId, userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found with id: " + cartItemId));

        // Validate ownership
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to remove this cart item");
        }

        cartItemRepository.delete(cartItem);
        log.info("[CartService] Cart item removed: {}", cartItemId);

        // Reload cart
        Cart cart = cartRepository.findByUserIdWithItemsAndProducts(userId)
                .orElse(null);

        if (cart == null || cart.getCartItems().isEmpty()) {
            return CartResponse.builder()
                    .userId(userId)
                    .items(java.util.Collections.emptyList())
                    .totalItems(0)
                    .totalAmount(java.math.BigDecimal.ZERO)
                    .build();
        }

        return CartMapper.toCartResponse(cart);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @Override
    @Transactional
    public void clearCart(Long userId) {
        log.info("[CartService] Clearing cart for user {}", userId);

        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            log.info("[CartService] Cart cleared for user {}", userId);
        }
    }

    /**
     * Đếm số lượng items trong giỏ
     */
    @Override
    public int getCartItemCount(Long userId) {
        log.info("[CartService] Getting cart item count for user {}", userId);

        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return 0;
        }

        return cart.getTotalItems();
    }

    // ========== PRIVATE METHODS ==========

    /**
     * Get or create cart for user
     */
    private Cart getOrCreateCart(Long userId) {
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);

        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create new cart
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Cart newCart = Cart.builder()
                .user(user)
                .build();

        return cartRepository.save(newCart);
    }
}
