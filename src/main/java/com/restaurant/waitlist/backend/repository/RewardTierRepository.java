package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.RewardTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardTierRepository extends JpaRepository<RewardTier, Long> {
    List<RewardTier> findByRestaurantIdOrderByTierOrderAsc(Long restaurantId);

    Optional<RewardTier> findByRestaurantIdAndTierOrder(Long restaurantId, Integer tierOrder);

    @Query("SELECT t FROM RewardTier t WHERE t.restaurant.id = :restaurantId " +
            "AND t.pointsThreshold <= :points ORDER BY t.tierOrder DESC LIMIT 1")
    Optional<RewardTier> findTierForPoints(Long restaurantId, Long points);

    List<RewardTier> findByRestaurantId(Long restaurantId);
}
