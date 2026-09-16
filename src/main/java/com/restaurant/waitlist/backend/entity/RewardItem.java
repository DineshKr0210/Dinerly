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
@jakarta.persistence.Table(name = "reward_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String title; // "Free coffee", "$5 off"

    @Column(columnDefinition = "TEXT")
    private String description; // "Any size, any blend"

    @Column(nullable = false, name = "points_cost")
    private Long pointsCost; // Cost in points

    @Column(name = "icon")
    private String icon; // For UI

    @Column(name = "category")
    private String category; // "food", "beverage", "discount"

    @Column(name = "available")
    @Builder.Default
    private Boolean available = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
