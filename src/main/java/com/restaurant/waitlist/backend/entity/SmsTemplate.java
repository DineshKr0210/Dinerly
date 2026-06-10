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
@jakarta.persistence.Table(name = "sms_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String templateType;  // e.g., "WAITLIST_NOTIFICATION", "SEATED_NOTIFICATION"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;  // Template with placeholders like {guestName}, {estimatedWait}, {position}

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TemplateType {
        WAITLIST_NOTIFICATION,  // For notifying guests their table is ready
        SEATED_NOTIFICATION     // For confirming guest is seated
    }
}


