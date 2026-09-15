package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Page<Campaign> findByRestaurantId(Long restaurantId, Pageable pageable);
    Page<Campaign> findByStatus(String status, Pageable pageable);
    Page<Campaign> findByChannel(String channel, Pageable pageable);
    Page<Campaign> findByRestaurantIdAndStatus(Long restaurantId, String status, Pageable pageable);
    Page<Campaign> findByRestaurantIdAndChannel(Long restaurantId, String channel, Pageable pageable);
    Page<Campaign> findByStatusAndChannel(String status, String channel, Pageable pageable);
    Page<Campaign> findByRestaurantIdAndStatusAndChannel(Long restaurantId, String status, String channel, Pageable pageable);

    long countByRestaurantId(Long restaurantId);
    long countByStatus(String status);
    long countByRestaurantIdAndStatus(Long restaurantId, String status);

    java.util.List<Campaign> findByRestaurantId(Long restaurantId);
    java.util.List<Campaign> findAllByOrderByCreatedAtDesc();

    org.springframework.data.domain.Page<Campaign> findByStatusAndScheduledAtBefore(String status, java.time.LocalDateTime before, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT DATE(c.created_at) AS day, COALESCE(SUM(c.redemptions), 0) AS redemptions " +
            "FROM campaigns c " +
            "WHERE (:restaurantId IS NULL OR c.restaurant_id = :restaurantId) " +
            "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(c.created_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(c.created_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY DATE(c.created_at) ORDER BY DATE(c.created_at)", nativeQuery = true)
    java.util.List<Object[]> aggregateRedemptionsByDay(Long restaurantId, java.sql.Date fromDate, java.sql.Date toDate);
}
