package com.training.demo.service.impl;

import com.training.demo.dto.request.Order.CheckoutCartRequest;
import com.training.demo.dto.request.Order.CreateOrderRequest;
import com.training.demo.dto.request.Order.OrderItemRequest;
import com.training.demo.dto.request.Order.UpdateOrderStatusRequest;
import com.training.demo.dto.response.Order.OrderItemResponse;
import com.training.demo.dto.response.Order.OrderResponse;
import com.training.demo.dto.response.System.PageResponse;
import com.training.demo.entity.*;
import com.training.demo.utils.enums.PaymentMethod;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.mapper.OrderMapper;
import com.training.demo.producer.EmailProducer;
import com.training.demo.repository.OrderRepository;
import com.training.demo.repository.ProductRepository;
import com.training.demo.repository.UserRepository;
import com.training.demo.repository.CartRepository;
import com.training.demo.repository.PaymentRepository;
import com.training.demo.service.OrderService;
import com.training.demo.utils.enums.OrderStatus;
import com.training.demo.utils.enums.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;
    private final EmailProducer emailProducer;

    /**
     * Tạo đơn hàng mới
     */
    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        log.info("[OrderService] Creating order for user: {}", userId);

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Validate items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must have at least one item");
        }

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .phoneNumber(request.getPhoneNumber())
                .receiverName(request.getReceiverName())
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .orderItems(new ArrayList<>())
                .build();

        // Process order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found with id: " + itemReq.getProductId()));

            // Check stock
            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Product '" + product.getName() + "' is out of stock. Available: " + product.getQuantity());
            }

            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .quantity(itemReq.getQuantity())
                    .price(product.getPrice())
                    .productImageUrl(product.getProductImageUrl())
                    .build();
            orderItem.calculateSubtotal();
            order.addOrderItem(orderItem);

            // Decrease product quantity
            product.setQuantity(product.getQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // Tạo Payment record cho COD (để tracking)
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.COD)
                    .amount(order.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .paymentInfo("Cash on Delivery - Payment will be collected upon delivery")
                    .build();
            paymentRepository.save(payment);
            log.info("[OrderService] Created COD payment record for order: {}", order.getOrderNumber());
        }

        log.info("[OrderService] Order created successfully: {}", order.getOrderNumber());

        // Send email confirmation via RabbitMQ queue
        try {
            // Convert entities to DTOs to avoid Jackson serialization issues
            List<OrderItemResponse> itemDTOs = order.getOrderItems().stream()
                    .map(item -> OrderItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProductName())
                            .productImageUrl(item.getProductImageUrl())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subtotal(item.getSubtotal())
                            .build())
                    .toList();
            
            java.util.Map<String, Object> emailData = new java.util.HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("receiverName", order.getReceiverName());
            emailData.put("totalAmount", order.getTotalAmount());
            emailData.put("shippingAddress", order.getShippingAddress());
            emailData.put("items", itemDTOs);
            
            emailProducer.sendOrderConfirmationEmail(user.getEmail(), emailData);
        } catch (Exception e) {
            log.error("[OrderService] Failed to queue order confirmation email", e);
        }

        return OrderMapper.toOrderResponse(order);
    }

    /**
     * Checkout từ giỏ hàng
     */
    @Override
    @Transactional
    public OrderResponse checkoutCart(Long userId, CheckoutCartRequest request) {
        log.info("[OrderService] Checkout cart for user: {}", userId);

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Get cart with items
        Cart cart = cartRepository.findByUserIdWithItemsAndProducts(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + userId));

        // Validate cart not empty
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Please add items before checkout");
        }

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .phoneNumber(request.getPhoneNumber())
                .receiverName(request.getReceiverName())
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .orderItems(new ArrayList<>())
                .build();

        // Process cart items -> order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            // Check stock
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Product '" + product.getName() + "' is out of stock. Available: " + product.getQuantity() + ", in cart: " + cartItem.getQuantity());
            }

            // Create order item from cart item
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice()) // Use current price, not priceAtAdd
                    .productImageUrl(product.getProductImageUrl())
                    .build();
            orderItem.calculateSubtotal();
            order.addOrderItem(orderItem);

            // Decrease product quantity
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // Tạo Payment record cho COD (để tracking)
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.COD)
                    .amount(order.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .paymentInfo("Cash on Delivery - Payment will be collected upon delivery")
                    .build();
            paymentRepository.save(payment);
            log.info("[OrderService] Created COD payment record for order: {}", order.getOrderNumber());
        }

        // Clear cart after successful checkout
        cart.clearItems();
        cartRepository.save(cart);

        log.info("[OrderService] Order created from cart successfully: {}", order.getOrderNumber());

        // Send email confirmation via RabbitMQ queue
        try {
            // Convert entities to DTOs to avoid Jackson serialization issues
            List<OrderItemResponse> itemDTOs = order.getOrderItems().stream()
                    .map(item -> OrderItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProductName())
                            .productImageUrl(item.getProductImageUrl())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subtotal(item.getSubtotal())
                            .build())
                    .toList();
            
            java.util.Map<String, Object> emailData = new java.util.HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("receiverName", order.getReceiverName());
            emailData.put("totalAmount", order.getTotalAmount());
            emailData.put("shippingAddress", order.getShippingAddress());
            emailData.put("items", itemDTOs);
            
            emailProducer.sendOrderConfirmationEmail(user.getEmail(), emailData);
        } catch (Exception e) {
            log.error("[OrderService] Failed to queue order confirmation email", e);
        }

        return OrderMapper.toOrderResponse(order);
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    @Override
    public OrderResponse getOrderById(Long orderId, Long userId) {
        log.info("[OrderService] Getting order: {} for user: {}", orderId, userId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Validate ownership
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to view this order");
        }

        return OrderMapper.toOrderResponse(order);
    }

    /**
     * Lấy danh sách đơn hàng của user
     */
    @Override
    public PageResponse<OrderResponse> getUserOrders(Long userId, OrderStatus status, Pageable pageable) {
        log.info("[OrderService] Getting orders for user: {}, status: {}", userId, status);

        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        List<OrderResponse> content = orderPage.getContent()
                .stream()
                .map(OrderMapper::toOrderResponse)
                .toList();

        return PageResponse.<OrderResponse>builder()
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .content(content)
                .build();
    }

    /**
     * Hủy đơn hàng
     */
    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        log.info("[OrderService] Canceling order: {} by user: {}", orderId, userId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Validate ownership
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to cancel this order");
        }

        // Check status - chỉ hủy được khi PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Cannot cancel order with status: " + order.getStatus());
        }

        // Restore product quantity
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        log.info("[OrderService] Order cancelled successfully: {}", order.getOrderNumber());

        // Send cancellation email via RabbitMQ queue
        try {
            java.util.Map<String, Object> emailData = new java.util.HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("receiverName", order.getReceiverName());
            emailData.put("totalAmount", order.getTotalAmount());
            
            emailProducer.sendOrderCancelledEmail(order.getUser().getEmail(), emailData);
        } catch (Exception e) {
            log.error("[OrderService] Failed to queue order cancellation email", e);
        }

        return OrderMapper.toOrderResponse(order);
    }

    /**
     * Cập nhật trạng thái đơn hàng (ADMIN)
     */
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        log.info("[OrderService] Updating order status: {} to {}", orderId, request.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());
        if (request.getNote() != null && !request.getNote().isEmpty()) {
            order.setNote(order.getNote() + "\n[Admin] " + request.getNote());
        }

        order = orderRepository.save(order);

        log.info("[OrderService] Order status updated: {} -> {}", orderId, request.getStatus());

        // Send status update email via RabbitMQ queue
        try {
            java.util.Map<String, Object> emailData = new java.util.HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("receiverName", order.getReceiverName());
            emailData.put("status", order.getStatus());
            emailData.put("totalAmount", order.getTotalAmount());
            
            emailProducer.sendOrderStatusUpdateEmail(order.getUser().getEmail(), emailData);
        } catch (Exception e) {
            log.error("[OrderService] Failed to queue order status update email", e);
        }

        return OrderMapper.toOrderResponse(order);
    }

    /**
     * Lấy tất cả đơn hàng (ADMIN)
     */
    @Override
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        log.info("[OrderService] Getting all orders, status: {}", status);

        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }

        List<OrderResponse> content = orderPage.getContent()
                .stream()
                .map(OrderMapper::toOrderResponse)
                .toList();

        return PageResponse.<OrderResponse>builder()
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .content(content)
                .build();
    }

    /**
     * Đếm số đơn hàng của user
     */
    @Override
    public long countUserOrders(Long userId) {
        log.info("[OrderService] Counting orders for user: {}", userId);
        return orderRepository.countByUserId(userId);
    }

    /**
     * Lấy order theo order number
     */
    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.info("[OrderService] Getting order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order not found with number: " + orderNumber));

        return OrderMapper.toOrderResponse(order);
    }

    // ========== PRIVATE METHODS ==========

    /**
     * Generate unique order number
     * Format: ORD-YYYYMMDD-HHMMSS-XXX
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String random = String.format("%03d", (int) (Math.random() * 1000));
        String orderNumber = "ORD-" + timestamp + "-" + random;

        // Check uniqueness
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            random = String.format("%03d", (int) (Math.random() * 1000));
            orderNumber = "ORD-" + timestamp + "-" + random;
        }

        return orderNumber;
    }

    /**
     * Đếm tổng số đơn hàng (ADMIN)
     */
    @Override
    public long countAllOrders() {
        return orderRepository.count();
    }

    /**
     * Đếm đơn hàng theo trạng thái (ADMIN)
     */
    @Override
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        // CANCELLED và REFUNDED không thể chuyển sang trạng thái khác
        if (current == OrderStatus.CANCELLED || current == OrderStatus.REFUNDED) {
            throw new BadRequestException("Cannot change status from " + current);
        }

        // DELIVERED chỉ có thể chuyển sang REFUNDED
        if (current == OrderStatus.DELIVERED && target != OrderStatus.REFUNDED) {
            throw new BadRequestException("Delivered order can only be refunded");
        }

        // Không thể quay lại PENDING
        if (target == OrderStatus.PENDING && current != OrderStatus.PENDING) {
            throw new BadRequestException("Cannot change status back to PENDING");
        }
    }
}
