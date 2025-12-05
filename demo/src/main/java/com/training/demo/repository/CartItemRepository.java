package com.training.demo.repository;

import com.training.demo.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Lấy tất cả items của cart
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Tìm item trong cart theo product
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Kiểm tra product đã có trong cart chưa
     */
    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Đếm số lượng items trong cart
     */
    long countByCartId(Long cartId);

    /**
     * Xóa tất cả items của cart
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

    /**
     * Xóa item theo cart và product
     */
    void deleteByCartIdAndProductId(Long cartId, Long productId);
}
