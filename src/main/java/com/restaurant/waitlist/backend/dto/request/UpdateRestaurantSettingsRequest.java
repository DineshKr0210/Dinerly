package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateRestaurantSettingsRequest {
    private Boolean sendSmsNotifications;
    private Boolean sendEmailNotifications;
    private String nightlySummaryEmail;

    @Positive(message = "Average service time must be positive")
    private Integer averageServiceTime;

    @Positive(message = "Buffer time must be positive")
    private Integer bufferTime;

    private String operatingHours;

    @Positive(message = "Max waitlist size must be positive")
    private Integer maxWaitlistSize;
}

