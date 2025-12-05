package com.training.demo.repository;

import com.training.demo.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Lấy tất cả items của một order
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Lấy items theo product
     */
    List<OrderItem> findByProductId(Long productId);

    /**
     * Đếm số lượng đã bán của một product
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long countTotalSoldByProductId(@Param("productId") Long productId);

    /**
     * Xóa tất cả items của order
     */
    void deleteByOrderId(Long orderId);
}
