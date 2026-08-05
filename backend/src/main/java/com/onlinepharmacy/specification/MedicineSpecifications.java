package com.onlinepharmacy.specification;

import com.onlinepharmacy.entity.Inventory;
import com.onlinepharmacy.entity.Medicine;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds dynamic JPA criteria for the medicine catalogue search/filter.
 * Every filter is optional and combined with AND (except within-name search which is OR).
 */
public final class MedicineSpecifications {

    private MedicineSpecifications() {
    }

    public static Specification<Medicine> withCriteria(String q, Long categoryId,
                                                       BigDecimal minPrice, BigDecimal maxPrice,
                                                       Boolean prescriptionRequired, Boolean inStock) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("active")));

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("genericName")), like),
                        cb.like(cb.lower(root.get("brandName")), like)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (prescriptionRequired != null) {
                predicates.add(cb.equal(root.get("prescriptionRequired"), prescriptionRequired));
            }

            if (Boolean.TRUE.equals(inStock)) {
                Join<Medicine, Inventory> inventory = root.join("inventory", JoinType.LEFT);
                predicates.add(cb.greaterThan(
                        inventory.get("availableQuantity"), inventory.get("reservedQuantity")));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}