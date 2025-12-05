package com.training.demo.repository;

import com.training.demo.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Tìm cart theo user ID
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Kiểm tra user đã có cart chưa
     */
    boolean existsByUserId(Long userId);

    /**
     * Lấy cart với cart items
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Lấy cart với cart items và products
     */
    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.cartItems ci " +
           "LEFT JOIN FETCH ci.product " +
           "WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItemsAndProducts(@Param("userId") Long userId);

    /**
     * Xóa cart của user
     */
    void deleteByUserId(Long userId);
}
