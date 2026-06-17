package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportsResponse {
    private long totalGuests;
    private long totalWaiting;
    private long totalNotified;
    private long totalSeated;
    private long totalCancelled;
    private int averageWaitTime; // minutes rounded
    private long todayGuestsCount;
    private long todaySeatedCount;
}

