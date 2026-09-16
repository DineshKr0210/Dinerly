package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.RewardItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardItemRepository extends JpaRepository<RewardItem, Long> {
    Page<RewardItem> findByRestaurantId(Long restaurantId, Pageable pageable);

    List<RewardItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    List<RewardItem> findByRestaurantIdAndCategoryAndAvailableTrue(Long restaurantId, String category);
}
