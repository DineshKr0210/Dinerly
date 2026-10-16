package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.ServerFloorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServerFloorStatusRepository extends JpaRepository<ServerFloorStatus, Long> {
    Optional<ServerFloorStatus> findByStaffId(Long staffId);

    List<ServerFloorStatus> findByRestaurantIdOrderByRotationOrderAsc(Long restaurantId);

    List<ServerFloorStatus> findByRestaurantIdAndOnFloorTrueOrderByRotationOrderAsc(Long restaurantId);

    Optional<ServerFloorStatus> findByRestaurantIdAndIsNextUpTrue(Long restaurantId);
}

