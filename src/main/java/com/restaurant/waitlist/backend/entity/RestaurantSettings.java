package com.restaurant.waitlist.backend.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "restaurant_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "restaurant_id", nullable = false, unique = true)
    private Restaurant restaurant;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sendSmsNotifications = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sendEmailNotifications = true;

    private String nightlySummaryEmail;

    @Column(nullable = false)
    @Builder.Default
    private Integer averageServiceTime = 45;

    @Column(nullable = false)
    @Builder.Default
    private Integer bufferTime = 15;

    @Column(nullable = false)
    @Builder.Default
    private String operatingHours = "10:00-22:00";

    @Column(nullable = false)
    @Builder.Default
    private Integer maxWaitlistSize = 50;

    @Column(columnDefinition = "TEXT")
    private String notificationSettingsJson;

    @Column(columnDefinition = "TEXT")
    private String waitlistSettingsJson;

    @Column(columnDefinition = "TEXT")
    private String holidayHoursJson;

    @Column(columnDefinition = "TEXT")
    private String advancedSettingsJson;

    @Transient
    private NotificationSettingsPayload notificationSettings;

    @Transient
    private WaitlistSettingsPayload waitlistSettings;

    @Transient
    private HolidayHoursPayload holidayHours;

    @Transient
    private AdvancedSettingsPayload advancedSettings;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public NotificationSettingsPayload getNotificationSettings() {
        if (notificationSettings != null) {
            return notificationSettings;
        }
        if (notificationSettingsJson == null || notificationSettingsJson.isBlank()) {
            notificationSettings = NotificationSettingsPayload.defaults();
            return notificationSettings;
        }
        try {
            notificationSettings = new ObjectMapper().readValue(notificationSettingsJson, NotificationSettingsPayload.class);
        } catch (JsonProcessingException e) {
            notificationSettings = NotificationSettingsPayload.defaults();
        }
        return notificationSettings;
    }

    public void setNotificationSettings(NotificationSettingsPayload notificationSettings) {
        this.notificationSettings = notificationSettings;
        try {
            this.notificationSettingsJson = new ObjectMapper().writeValueAsString(notificationSettings);
        } catch (JsonProcessingException e) {
            this.notificationSettingsJson = null;
        }
    }

    public WaitlistSettingsPayload getWaitlistSettings() {
        if (waitlistSettings != null) {
            return waitlistSettings;
        }
        if (waitlistSettingsJson == null || waitlistSettingsJson.isBlank()) {
            waitlistSettings = WaitlistSettingsPayload.defaults();
            return waitlistSettings;
        }
        try {
            waitlistSettings = new ObjectMapper().readValue(waitlistSettingsJson, WaitlistSettingsPayload.class);
        } catch (JsonProcessingException e) {
            waitlistSettings = WaitlistSettingsPayload.defaults();
        }
        return waitlistSettings;
    }

    public void setWaitlistSettings(WaitlistSettingsPayload waitlistSettings) {
        this.waitlistSettings = waitlistSettings;
        try {
            this.waitlistSettingsJson = new ObjectMapper().writeValueAsString(waitlistSettings);
        } catch (JsonProcessingException e) {
            this.waitlistSettingsJson = null;
        }
    }

    public HolidayHoursPayload getHolidayHours() {
        if (holidayHours != null) {
            return holidayHours;
        }
        if (holidayHoursJson == null || holidayHoursJson.isBlank()) {
            holidayHours = HolidayHoursPayload.defaults();
            return holidayHours;
        }
        try {
            holidayHours = new ObjectMapper().readValue(holidayHoursJson, HolidayHoursPayload.class);
        } catch (JsonProcessingException e) {
            holidayHours = HolidayHoursPayload.defaults();
        }
        return holidayHours;
    }

    public void setHolidayHours(HolidayHoursPayload holidayHours) {
        this.holidayHours = holidayHours;
        try {
            this.holidayHoursJson = new ObjectMapper().writeValueAsString(holidayHours);
        } catch (JsonProcessingException e) {
            this.holidayHoursJson = null;
        }
    }

    public AdvancedSettingsPayload getAdvancedSettings() {
        if (advancedSettings != null) {
            return advancedSettings;
        }
        if (advancedSettingsJson == null || advancedSettingsJson.isBlank()) {
            advancedSettings = AdvancedSettingsPayload.defaults();
            return advancedSettings;
        }
        try {
            advancedSettings = new ObjectMapper().readValue(advancedSettingsJson, AdvancedSettingsPayload.class);
        } catch (JsonProcessingException e) {
            advancedSettings = AdvancedSettingsPayload.defaults();
        }
        return advancedSettings;
    }

    public void setAdvancedSettings(AdvancedSettingsPayload advancedSettings) {
        this.advancedSettings = advancedSettings;
        try {
            this.advancedSettingsJson = new ObjectMapper().writeValueAsString(advancedSettings);
        } catch (JsonProcessingException e) {
            this.advancedSettingsJson = null;
        }
    }
}

