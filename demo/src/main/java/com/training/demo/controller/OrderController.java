package com.training.demo.controller;

import com.training.demo.dto.request.Order.CheckoutCartRequest;
import com.training.demo.dto.request.Order.CreateOrderRequest;
import com.training.demo.dto.request.Order.UpdateOrderStatusRequest;
import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.security.SecurityUtils;
import com.training.demo.service.OrderService;
import com.training.demo.utils.enums.OrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Create new order (direct, not from cart)
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("[OrderController] Creating order");
        Long userId = SecurityUtils.getCurrentUserId();
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(orderService.createOrder(userId, request)));
    }

    /**
     * Checkout from shopping cart
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutCart(@Valid @RequestBody CheckoutCartRequest request) {
        log.info("[OrderController] Checkout cart");
        Long userId = SecurityUtils.getCurrentUserId();
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(orderService.checkoutCart(userId, request)));
    }

    /**
     * Get current user's order list
     */
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("[OrderController] Getting my orders, status: {}", status);
        Long userId = SecurityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(BaseResponse.success(
                orderService.getUserOrders(userId, status, pageable)
        ));
    }

    /**
     * Get order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        log.info("[OrderController] Getting order: {}", orderId);
        Long userId = SecurityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(BaseResponse.success(
                orderService.getOrderById(orderId, userId)
        ));
    }

    /**
     * Get order by order number
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<?> getOrderByNumber(@PathVariable String orderNumber) {
        log.info("[OrderController] Getting order by number: {}", orderNumber);
        
        return ResponseEntity.ok(BaseResponse.success(
                orderService.getOrderByOrderNumber(orderNumber)
        ));
    }

    /**
     * Cancel order
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        log.info("[OrderController] Canceling order: {}", orderId);
        Long userId = SecurityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(BaseResponse.success(
                orderService.cancelOrder(orderId, userId)
        ));
    }

    /**
     * Count user's orders
     */
    @GetMapping("/count")
    public ResponseEntity<?> countMyOrders() {
        log.info("[OrderController] Counting my orders");
        Long userId = SecurityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(BaseResponse.success(
                orderService.countUserOrders(userId)
        ));
    }

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Get all orders (ADMIN)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("[OrderController] Admin getting all orders, status: {}", status);
        
        return ResponseEntity.ok(BaseResponse.success(
                orderService.getAllOrders(status, pageable)
        ));
    }

    /**
     * Update order status (ADMIN)
     */
    @PatchMapping("/admin/{orderId}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("[OrderController] Admin updating order status: {} to {}", orderId, request.getStatus());
        
        return ResponseEntity.ok(BaseResponse.success(
                orderService.updateOrderStatus(orderId, request)
        ));
    }

    /**
     * Count total orders (ADMIN)
     */
    @GetMapping("/admin/count")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> countAllOrders() {
        log.info("[OrderController] Admin counting all orders");
        
        return ResponseEntity.ok(BaseResponse.success(
                orderService.countAllOrders()
        ));
    }

    /**
     * Count orders by status (ADMIN)
     */
    @GetMapping("/admin/count/status/{status}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> countOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("[OrderController] Admin counting orders by status: {}", status);
        
        return ResponseEntity.ok(BaseResponse.success(
                orderService.countOrdersByStatus(status)
        ));
    }
}
