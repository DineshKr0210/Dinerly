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
@jakarta.persistence.Table(name = "waitlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(name = "guest_phone", nullable = false)
    private String guestPhone;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    private String preference;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WaitlistStatus status = WaitlistStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_active_status")
    private WaitlistStatus lastActiveStatus;

    @Column(name = "position")
    private Integer position;

    @Column(name = "estimated_wait_time")
    private Integer estimatedWaitTime;

    @Column(name = "joined_at")
    @CreationTimestamp
    private LocalDateTime joinedAt;

    @Column(name = "seated_at")
    private LocalDateTime seatedAt;

    @Column(name = "sms_message")
    private String smsMessage;

    @Column(name = "sms_status")
    private String smsStatus; // SENT / FAILED

    @Column(name = "sms_error")
    private String smsError;

    @Column(name = "sms_sent_at")
    private LocalDateTime smsSentAt;

    @Column(name = "latest_customer_reply")
    private String latestCustomerReply;

    @Column(name = "customer_reply_received_at")
    private LocalDateTime customerReplyReceivedAt;

    @Column(name = "customer_reply_sid")
    private String customerReplySid;

    @Column(name = "latest_voice_reply")
    private String latestVoiceReply;

    @Column(name = "voice_reply_received_at")
    private LocalDateTime voiceReplyReceivedAt;

    @Column(name = "voice_reply_digits")
    private String voiceReplyDigits;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "table_name")
    private String tableName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum WaitlistStatus {
        PENDING,      // Guest has joined, awaiting restaurant approval
        WAITING,      // Restaurant has approved, guest is waiting
        NOTIFIED,     // Guest has been notified to come
        SEATED,       // Guest has been seated
        CANCELLED,    // Guest cancelled
        NO_SHOW       // Guest didn't show up
    }
}

