package com.restaurant.waitlist.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackInsightsResponse {
    private Double overallRating;
    private Long totalReviews;
    private List<String> topTags;
}

