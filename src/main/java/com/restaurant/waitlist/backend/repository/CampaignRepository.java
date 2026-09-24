package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    // Franchise-group scoped variants: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    Page<Campaign> findByRestaurantIdIn(List<Long> restaurantIds, Pageable pageable);
    Page<Campaign> findByRestaurantIdInAndStatus(List<Long> restaurantIds, String status, Pageable pageable);
    List<Campaign> findByRestaurantIdInAndCreatedAtBetween(List<Long> restaurantIds, java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Campaign c WHERE c.restaurantId IN :restaurantIds AND c.channels LIKE CONCAT('%', :channel, '%')")
    Page<Campaign> findByRestaurantIdInAndChannel(List<Long> restaurantIds, String channel, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Campaign c WHERE c.restaurantId IN :restaurantIds AND c.status = :status AND c.channels LIKE CONCAT('%', :channel, '%')")
    Page<Campaign> findByRestaurantIdInAndStatusAndChannel(List<Long> restaurantIds, String status, String channel, Pageable pageable);

    java.util.List<Campaign> findByRestaurantId(Long restaurantId);

    org.springframework.data.domain.Page<Campaign> findByStatusAndScheduledAtBefore(String status, java.time.LocalDateTime before, org.springframework.data.domain.Pageable pageable);

    // Franchise-group scoped variant: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    @org.springframework.data.jpa.repository.Query(value = "SELECT DATE(c.created_at) AS day, COALESCE(SUM(c.redemptions), 0) AS redemptions " +
            "FROM campaigns c " +
            "WHERE c.restaurant_id IN (:restaurantIds) " +
            "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(c.created_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(c.created_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY DATE(c.created_at) ORDER BY DATE(c.created_at)", nativeQuery = true)
    java.util.List<Object[]> aggregateRedemptionsByDayForRestaurantIds(List<Long> restaurantIds, java.sql.Date fromDate, java.sql.Date toDate);

    List<Campaign> findByRestaurantIdIn(List<Long> restaurantIds);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(c) FROM Campaign c WHERE c.restaurantId = :restaurantId AND c.status = 'ACTIVE'")
    long countActiveCampaignsByRestaurant(Long restaurantId);

    // Shared campaign coupon codes: looked up at POS validation time and
    // checked for uniqueness before a campaign is published.
    java.util.Optional<Campaign> findByRedemptionCode(String redemptionCode);
    boolean existsByRedemptionCode(String redemptionCode);
}
