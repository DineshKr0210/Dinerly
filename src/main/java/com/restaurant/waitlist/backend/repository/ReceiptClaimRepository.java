package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.ReceiptClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReceiptClaimRepository extends JpaRepository<ReceiptClaim, Long> {
    Page<ReceiptClaim> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<ReceiptClaim> findByRestaurantIdAndStatus(Long restaurantId, ReceiptClaim.ClaimStatus status, Pageable pageable);

    List<ReceiptClaim> findByUserIdAndRestaurantIdAndCreatedAtAfter(Long userId, Long restaurantId, LocalDateTime createdAfter);

    // Franchise-group scoped variants: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    Page<ReceiptClaim> findByRestaurantIdIn(List<Long> restaurantIds, Pageable pageable);

    Page<ReceiptClaim> findByRestaurantIdInAndStatus(List<Long> restaurantIds, ReceiptClaim.ClaimStatus status, Pageable pageable);

    Page<ReceiptClaim> findByRestaurantIdInAndUserId(List<Long> restaurantIds, Long userId, Pageable pageable);
}
