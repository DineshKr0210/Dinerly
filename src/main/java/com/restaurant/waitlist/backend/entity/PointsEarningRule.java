package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "points_earning_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsEarningRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String action; // "dine_in", "join_waitlist", "leave_review", "refer_friend"

    @Column(nullable = false, name = "points_value")
    private Long pointsValue; // Points awarded

    @Column(columnDefinition = "TEXT")
    private String description; // For display

    @Column(name = "icon")
    private String icon; // For UI

    @Column(name = "clickable")
    @Builder.Default
    private Boolean clickable = false; // If true, triggers in-app action

    @Column(name = "action_url")
    private String actionUrl; // URL to navigate to if clickable

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
