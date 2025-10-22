package com.smartvn.product_service.specification;

import com.smartvn.product_service.model.Inventory;
import com.smartvn.product_service.model.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> searchProducts(
            String keyword,
            List<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: isActive = true
            predicates.add(cb.isTrue(root.get("isActive")));

            // Keyword search
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + keyword.toLowerCase() + "%"
                ));
            }

            // Category filter
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            // Price filter - JOIN với Inventory
            if (minPrice != null || maxPrice != null) {
                Join<Product, Inventory> inventoryJoin = root.join("inventories", JoinType.INNER);

                if (minPrice != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                            inventoryJoin.get("discountedPrice"), minPrice
                    ));
                }

                if (maxPrice != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                            inventoryJoin.get("discountedPrice"), maxPrice
                    ));
                }
            }

            // DISTINCT để tránh duplicate khi JOIN
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}