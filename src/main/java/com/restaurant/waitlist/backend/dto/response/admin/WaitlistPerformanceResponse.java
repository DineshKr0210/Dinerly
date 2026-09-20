package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistPerformanceResponse {
    private Summary summary;
    private List<TrendPoint> trend;
    private List<LeaderboardEntry> leaderboard;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long waitlistJoins;
        private long prevWaitlistJoins;
        private double waitlistJoinsChangePercent;
        private long guestsSeated;
        private long prevGuestsSeated;
        private double guestsSeatedChangePercent;
        private double averageWaitTimeMinutes;
        private double prevAverageWaitTimeMinutes;
        private double avgWaitChangePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String date;
        private String label;
        private long value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {
        private int rank;
        private String location;
        private long waitlistJoins;
        private long redemptions;
        private long avgWaitMinutes;
        private double rating;
    }
}
