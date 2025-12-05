package com.training.demo.repository.custom;

import com.training.demo.dto.response.Product.ProductResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<ProductResponse> searchByName(String name) {
        Query query = em.createNativeQuery("""
            SELECT
                p.id AS id,
                p.name AS name,
                p.description AS description,
                p.price AS price,
                p.quantity AS quantity,
                p.product_image_url AS productImageUrl
            FROM tbl_product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
        """, "Mapping.ProductResponse"); // ← dùng mapping bạn đã định nghĩa

        query.setParameter("name", name);

        return query.getResultList();
    }
}

