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
@jakarta.persistence.Table(name = "staff")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffRole role;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true, length = 255)
    private String password;

    // ✅ NEW: Link to User record for authentication
    @Column(name = "user_id", unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private StaffStatus status = StaffStatus.ACTIVE;

    public enum StaffStatus {
        ACTIVE, INVITED, INACTIVE
    }

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
