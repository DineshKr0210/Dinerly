package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query("SELECT r FROM Restaurant r WHERE r.locationOpen = false AND r.id = :restaurantId")
    Optional<Restaurant> findClosedLocationById(@Param("restaurantId") Long restaurantId);

    // A restaurant's own franchise group = itself plus every location whose
    // mainRestaurantId points back to it. For a standalone restaurant this
    // resolves to just itself, since nothing else references it as main.
    @Query("SELECT r FROM Restaurant r WHERE r.id = :restaurantId OR r.mainRestaurantId = :restaurantId")
    List<Restaurant> findByIdOrMainRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("SELECT r FROM Restaurant r WHERE r.id = :restaurantId OR r.mainRestaurantId = :restaurantId")
    Page<Restaurant> findByIdOrMainRestaurantId(@Param("restaurantId") Long restaurantId, Pageable pageable);
}

