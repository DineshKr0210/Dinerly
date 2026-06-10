package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WaitlistDashboardStatsResponse {
    private Long totalWaiting;
    private Integer averageWaitTime;
}
