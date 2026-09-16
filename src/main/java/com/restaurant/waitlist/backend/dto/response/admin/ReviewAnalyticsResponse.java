package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAnalyticsResponse {
    private Double averageRating;
    private Long totalReviews;
    private Long needsReplyCount;
    private Double replyRate;
    private Long ratingCount5Star;
    private Long ratingCount4Star;
    private Long ratingCount3Star;
    private Long ratingCount2Star;
    private Long ratingCount1Star;
    private String trend; // UP, DOWN, STABLE
    private Integer daysAnalyzed;
}
