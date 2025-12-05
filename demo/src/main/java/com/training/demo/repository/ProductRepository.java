package com.training.demo.repository;

import com.training.demo.dto.response.Product.ExportProductResponse;
import com.training.demo.dto.response.Product.ProductResponse;
import com.training.demo.entity.Product;
import com.training.demo.repository.custom.ProductRepositoryCustom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product>, ProductRepositoryCustom {

    /**
     * Check if a product exists by name
     *
     * @param name product name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Check if any product exists by category ID
     *
     * @param categoryId category ID
     * @return true if any product exists with the given category ID, false otherwise
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Get all products with their category names
     *
     * @return List of ExportProductResponse
     */
    @Query("""

            SELECT p.id AS id,
           p.name AS name,
           p.description AS description,
           p.price AS price,
           p.quantity AS quantity,
           c.name AS categoryName,
           p.createdAt as createdAt
    FROM Product p
    JOIN p.category c
    ON p.category.id = c.id
    WHERE (:name IS NULL OR :name = ''\s
    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
    """)
    List<ExportProductResponse> findAllProjectedWithCategory(@Param("name") String name);

    @Query("""
        SELECT new com.training.demo.dto.response.Product.ProductResponse(
            p.id,
            p.name,
            p.description,
            p.price,
            p.quantity,
            p.productImageUrl,
            c.id,
            c.name,
            c.description
        )
        FROM Product p
        JOIN p.category c
        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))
        """)
    Page<ProductResponse> searchByCategory(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("""
        SELECT new com.training.demo.dto.response.Product.ProductResponse(
            p.id,
            p.name,
            p.description,
            p.price,
            p.quantity,
            p.productImageUrl,
            c.id,
            c.name,
            c.description
        )
        FROM Product p
        JOIN p.category c
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<ProductResponse> findByStatus(String status, Pageable pageable);

    @Query("""
    SELECT COUNT(p)
    FROM Product p
    WHERE p.category.id = :categoryId
    """)
    long countByCategoryId(Long categoryId);
}
