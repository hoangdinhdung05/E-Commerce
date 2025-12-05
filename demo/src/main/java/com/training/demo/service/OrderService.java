package com.training.demo.service;

import com.training.demo.dto.request.Order.CheckoutCartRequest;
import com.training.demo.dto.request.Order.CreateOrderRequest;
import com.training.demo.dto.request.Order.UpdateOrderStatusRequest;
import com.training.demo.dto.response.Order.OrderResponse;
import com.training.demo.dto.response.System.PageResponse;
import com.training.demo.utils.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /**
     * Tạo đơn hàng mới
     */
    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    /**
     * Checkout từ giỏ hàng
     */
    OrderResponse checkoutCart(Long userId, CheckoutCartRequest request);

    /**
     * Lấy chi tiết đơn hàng
     */
    OrderResponse getOrderById(Long orderId, Long userId);

    /**
     * Lấy danh sách đơn hàng của user
     */
    PageResponse<OrderResponse> getUserOrders(Long userId, OrderStatus status, Pageable pageable);

    /**
     * Hủy đơn hàng
     */
    OrderResponse cancelOrder(Long orderId, Long userId);

    /**
     * Cập nhật trạng thái đơn hàng (ADMIN)
     */
    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    /**
     * Lấy tất cả đơn hàng (ADMIN)
     */
    PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable);

    /**
     * Đếm số đơn hàng của user
     */
    long countUserOrders(Long userId);

    /**
     * Lấy order theo order number
     */
    OrderResponse getOrderByOrderNumber(String orderNumber);

    /**
     * Đếm tổng số đơn hàng (ADMIN)
     */
    long countAllOrders();

    /**
     * Đếm đơn hàng theo trạng thái (ADMIN)
     */
    long countOrdersByStatus(OrderStatus status);
}
