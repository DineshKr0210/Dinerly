package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * In-app notification for a server, e.g. "You've been assigned Table 4 for a party of 2".
 * Polled by the server's own UI screen (GET /servers/me/notifications).
 */
@Entity
@jakarta.persistence.Table(name = "server_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "waitlist_id")
    private Long waitlistId;

    @Column(name = "table_id")
    private Long tableId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationType type = NotificationType.TABLE_ASSIGNED;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        TABLE_ASSIGNED,
        TABLE_CLEARED
    }
}

