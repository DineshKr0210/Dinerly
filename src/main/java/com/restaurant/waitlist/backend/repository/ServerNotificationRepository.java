package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.ServerNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerNotificationRepository extends JpaRepository<ServerNotification, Long> {
    Page<ServerNotification> findByRestaurantIdAndStaffIdOrderByCreatedAtDesc(Long restaurantId, Long staffId, Pageable pageable);

    long countByRestaurantIdAndStaffIdAndIsReadFalse(Long restaurantId, Long staffId);
}

