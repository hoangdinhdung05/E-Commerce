package com.training.demo.repository;

import com.training.demo.dto.response.Payment.ExportPaymentResponse;
import com.training.demo.entity.Payment;
import com.training.demo.utils.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Lấy tất cả payments của một order
     */
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    /**
     * Tìm payment theo transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Kiểm tra transaction ID đã tồn tại
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * Lấy payments theo status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Lấy payment thành công của order
     */
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.status = 'PAID' ORDER BY p.paymentDate DESC")
    List<Payment> findSuccessfulPaymentsByOrderId(@Param("orderId") Long orderId);

    /**
     * Đếm số lượng payment của order
     */
    long countByOrderId(Long orderId);

    /**
     * Tính tổng doanh thu từ các thanh toán thành công
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID'")
    java.math.BigDecimal calculateTotalRevenue();

    /**
     * Lấy danh sách payments cho export PDF
     */
    @Query("SELECT new com.training.demo.dto.response.Payment.ExportPaymentResponse(" +
           "p.id, p.order.id, p.order.orderNumber, p.order.user.username, " +
           "CAST(p.paymentMethod AS string), p.amount, CAST(p.status AS string), p.transactionId, " +
           "p.paymentDate, p.paymentInfo) " +
           "FROM Payment p " +
           "WHERE (:status IS NULL OR p.status = :status) " +
           "ORDER BY p.paymentDate DESC")
    List<ExportPaymentResponse> findAllForExport(@Param("status") PaymentStatus status);
}
