package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.RewardItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardItemRepository extends JpaRepository<RewardItem, Long> {
    List<RewardItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    // Franchise-group scoped variants: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    Page<RewardItem> findByRestaurantIdIn(List<Long> restaurantIds, Pageable pageable);

    Page<RewardItem> findByRestaurantIdInAndCategoryAndAvailableTrue(List<Long> restaurantIds, String category, Pageable pageable);
}
