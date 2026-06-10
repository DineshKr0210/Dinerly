package com.restaurant.waitlist.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {
    private Long totalSeated;
    private Double avgWaitTime;
    private Double noShowRate;
    private Double avgRating;
}

