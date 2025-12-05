package com.training.demo.repository.specification;

import com.training.demo.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.List;

public final class ProductSpecs {

    private ProductSpecs() {}

    public static Specification<Product> keyword(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String like = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }

    public static Specification<Product> categoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> categoryIds(List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) return null;
            CriteriaBuilder.In<Long> in = cb.in(root.get("category").get("id"));
            ids.forEach(in::value);
            return in;
        };
    }

    public static Specification<Product> minPrice(BigDecimal min) {
        return (root, query, cb) -> (min == null) ? null : cb.ge(root.get("price"), min);
    }

    public static Specification<Product> maxPrice(BigDecimal max) {
        return (root, query, cb) -> (max == null) ? null : cb.le(root.get("price"), max);
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return criteriaBuilder.conjunction(); // always true
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), min);
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), max);
            }
            return criteriaBuilder.between(root.get("price"), min, max);
        };
    }

    public static Specification<Product> inStock(Boolean inStock) {
        return (root, query, cb) -> {
            if (inStock == null) return null;
            return inStock ? cb.greaterThan(root.get("quantity"), 0) : null;
        };
    }
}
