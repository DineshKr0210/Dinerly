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
public class ReviewsPerformanceResponse {
    private Summary summary;
    private List<TrendPoint> trend;
    private List<LeaderboardEntry> leaderboard;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long reviewsReceived;
        private long prevReviewsReceived;
        private double reviewsReceivedChangePercent;
        private double averageRating;
        private double prevAverageRating;
        private double avgRatingChangePercent;
        private long replyRate;
        private long prevReplyRate;
        private double replyRateChangePercent;
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
        private long reviewsReceived;
        private double avgRating;
        private long replyRate;
    }
}
