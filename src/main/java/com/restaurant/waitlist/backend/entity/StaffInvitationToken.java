package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@jakarta.persistence.Table(name = "staff_invitation_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffInvitationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * Generate a unique token for staff invitation
     */
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Check if token is still valid (not expired and not used)
     */
    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiryDate);
    }
}

