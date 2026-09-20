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
public class RewardsOffersPerformanceResponse {
    private Summary summary;
    private List<TrendPoint> trend;
    private List<LeaderboardEntry> leaderboard;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long redemptions;
        private long prevRedemptions;
        private double redemptionsChangePercent;
        private long pointsIssued;
        private long prevPointsIssued;
        private double pointsIssuedChangePercent;
        private long activeOffers;
        private long prevActiveOffers;
        private double activeOffersChangePercent;
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
        private long redemptions;
        private long pointsIssued;
        private long activeOffers;
    }
}
