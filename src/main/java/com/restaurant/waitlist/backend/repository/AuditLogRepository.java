package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
}

