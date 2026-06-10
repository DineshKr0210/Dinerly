package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantSettingsRepository extends JpaRepository<RestaurantSettings, Long> {
    Optional<RestaurantSettings> findByRestaurantId(Long restaurantId);
    boolean existsByRestaurantId(Long restaurantId);
}

