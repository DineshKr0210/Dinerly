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
    Page<Campaign> findByRestaurantIdAndStatus(Long restaurantId, String status, Pageable pageable);

    long countByRestaurantId(Long restaurantId);
    long countByStatus(String status);
    long countByRestaurantIdAndStatus(Long restaurantId, String status);

    java.util.List<Campaign> findByRestaurantId(Long restaurantId);
    java.util.List<Campaign> findAllByOrderByCreatedAtDesc();

    org.springframework.data.domain.Page<Campaign> findByStatusAndScheduledAtBefore(String status, java.time.LocalDateTime before, org.springframework.data.domain.Pageable pageable);

    // Channel-based queries using JPQL for JSON array contains
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Campaign c WHERE c.channels LIKE CONCAT('%', :channel, '%')")
    Page<Campaign> findByChannel(String channel, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Campaign c WHERE c.restaurantId = :restaurantId AND c.channels LIKE CONCAT('%', :channel, '%')")
    Page<Campaign> findByRestaurantIdAndChannel(Long restaurantId, String channel, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Campaign c WHERE c.status = :status AND c.channels LIKE CONCAT('%', :channel, '%')")
    Page<Campaign> findByStatusAndChannel(String status, String channel, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Campaign c WHERE c.restaurantId = :restaurantId AND c.status = :status AND c.channels LIKE CONCAT('%', :channel, '%')")
    Page<Campaign> findByRestaurantIdAndStatusAndChannel(Long restaurantId, String status, String channel, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT DATE(c.created_at) AS day, COALESCE(SUM(c.redemptions), 0) AS redemptions " +
            "FROM campaigns c " +
            "WHERE (:restaurantId IS NULL OR c.restaurant_id = :restaurantId) " +
            "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(c.created_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(c.created_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY DATE(c.created_at) ORDER BY DATE(c.created_at)", nativeQuery = true)
    java.util.List<Object[]> aggregateRedemptionsByDay(Long restaurantId, java.sql.Date fromDate, java.sql.Date toDate);

    // Additional query methods for campaigns by date range
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Campaign c WHERE c.restaurantId = :restaurantId " +
            "AND c.createdAt >= :fromDate AND c.createdAt <= :toDate")
    java.util.List<Campaign> findByRestaurantIdAndDateRange(Long restaurantId, java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Campaign c WHERE c.createdAt >= :fromDate AND c.createdAt <= :toDate")
    java.util.List<Campaign> findByDateRange(java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(c) FROM Campaign c WHERE c.restaurantId = :restaurantId AND c.status = 'ACTIVE'")
    long countActiveCampaignsByRestaurant(Long restaurantId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(c) FROM Campaign c WHERE c.status = 'ACTIVE'")
    long countActiveCampaigns();
}
