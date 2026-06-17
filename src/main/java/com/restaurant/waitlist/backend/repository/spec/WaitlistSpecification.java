package com.restaurant.waitlist.backend.repository.spec;

import com.restaurant.waitlist.backend.entity.Waitlist;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class WaitlistSpecification {

    private WaitlistSpecification() {}

    public static Specification<Waitlist> filter(Long restaurantId,
                                                 Waitlist.WaitlistStatus status,
                                                 LocalDate date,
                                                 LocalDate fromDate,
                                                 LocalDate toDate) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();
            p = cb.and(p, cb.equal(root.get("restaurant").get("id"), restaurantId));

            if (status != null) {
                p = cb.and(p, cb.equal(root.get("status"), status));
            }

            if (date != null) {
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(23,59,59, 999_999_999);
                p = cb.and(p, cb.between(root.get("joinedAt"), start, end));
            } else {
                if (fromDate != null) {
                    LocalDateTime start = fromDate.atStartOfDay();
                    p = cb.and(p, cb.greaterThanOrEqualTo(root.get("joinedAt"), start));
                }
                if (toDate != null) {
                    LocalDateTime end = toDate.atTime(23,59,59, 999_999_999);
                    p = cb.and(p, cb.lessThanOrEqualTo(root.get("joinedAt"), end));
                }
            }
            return p;
        };
    }
}


