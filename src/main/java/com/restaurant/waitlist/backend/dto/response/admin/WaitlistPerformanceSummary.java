package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistPerformanceSummary {
    private long waitlistJoins;
    private String waitlistJoinsComparison;
    private long guestsSeated;
    private String guestsSeatedComparison;
    private Double averageWaitTimeMinutes;
    private String averageWaitTimeComparison;
}
