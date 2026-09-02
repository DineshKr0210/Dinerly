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
}
