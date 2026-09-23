package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.PointsEarningRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointsEarningRuleRepository extends JpaRepository<PointsEarningRule, Long> {
    List<PointsEarningRule> findByRestaurantId(Long restaurantId);

    Optional<PointsEarningRule> findByRestaurantIdAndAction(Long restaurantId, String action);

    // Franchise-group scoped variants: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    Page<PointsEarningRule> findByRestaurantIdIn(List<Long> restaurantIds, Pageable pageable);

    Page<PointsEarningRule> findByRestaurantIdInAndAction(List<Long> restaurantIds, String action, Pageable pageable);

    List<PointsEarningRule> findByRestaurantIdInAndAction(List<Long> restaurantIds, String action);
}
