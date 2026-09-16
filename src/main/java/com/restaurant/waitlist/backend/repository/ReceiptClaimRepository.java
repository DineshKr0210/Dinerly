package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.ReceiptClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptClaimRepository extends JpaRepository<ReceiptClaim, Long> {
    Page<ReceiptClaim> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<ReceiptClaim> findByRestaurantIdAndStatus(Long restaurantId, ReceiptClaim.ClaimStatus status, Pageable pageable);

    List<ReceiptClaim> findByUserIdAndRestaurantIdAndCreatedAtAfter(Long userId, Long restaurantId, LocalDateTime createdAfter);

    Optional<ReceiptClaim> findByUserIdAndRestaurantIdAndReceiptAmountAndReceiptDate(Long userId, Long restaurantId, String amount, String date);
}
