package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.PointsEarningRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointsEarningRuleRepository extends JpaRepository<PointsEarningRule, Long> {
    List<PointsEarningRule> findByRestaurantId(Long restaurantId);

    Optional<PointsEarningRule> findByRestaurantIdAndAction(Long restaurantId, String action);
}
