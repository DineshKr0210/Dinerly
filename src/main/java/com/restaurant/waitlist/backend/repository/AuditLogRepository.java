package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    // Pushes the activity-log filtering and pagination down to the DB instead of
    // loading the whole audit_logs table and filtering/paginating it in memory.
    Page<AuditLog> findByActionInOrderByIdDesc(List<String> actions, Pageable pageable);

    Page<AuditLog> findByActionContainingAndRestaurantIdInOrderByIdDesc(String actionFragment, List<Long> restaurantIds, Pageable pageable);
}

