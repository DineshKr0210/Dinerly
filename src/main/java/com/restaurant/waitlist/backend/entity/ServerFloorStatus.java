package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks the current floor-rotation state for a single server (Staff with role SERVER)
 * within a restaurant. One row per staff member. Reset only happens when a manager
 * explicitly starts a new shift (see ServerRotationService#startNewShift).
 */
@Entity
@jakarta.persistence.Table(name = "server_floor_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerFloorStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "staff_id", nullable = false, unique = true)
    private Long staffId;

    @Column(name = "on_floor", nullable = false)
    @Builder.Default
    private Boolean onFloor = false;

    @Column(name = "seated_count", nullable = false)
    @Builder.Default
    private Integer seatedCount = 0;

    // Lower number = earlier in the rotation queue. Unique ordering among on-floor servers.
    @Column(name = "rotation_order", nullable = false)
    @Builder.Default
    private Integer rotationOrder = 0;

    @Column(name = "is_next_up", nullable = false)
    @Builder.Default
    private Boolean isNextUp = false;

    @Column(name = "shift_started_at")
    private LocalDateTime shiftStartedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

