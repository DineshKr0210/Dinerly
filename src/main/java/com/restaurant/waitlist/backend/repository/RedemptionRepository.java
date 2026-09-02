package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Redemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    Page<Redemption> findByRestaurantId(Long restaurantId, Pageable pageable);

    @Query("SELECT r FROM Redemption r WHERE (:restaurantId IS NULL OR r.restaurantId = :restaurantId) " +
            "AND (:from IS NULL OR r.redeemedAt >= :from) AND (:to IS NULL OR r.redeemedAt <= :to)")
    Page<Redemption> findFiltered(Long restaurantId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
