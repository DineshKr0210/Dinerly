package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP, enabled = false WHERE id = ?")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum UserRole {
        GUEST, RESTAURANT, ADMIN
    }
}

