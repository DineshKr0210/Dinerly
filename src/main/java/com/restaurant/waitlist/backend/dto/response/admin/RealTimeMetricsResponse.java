package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RealTimeMetricsResponse {
    private Integer activeWaitlists;
    private Integer waitingCount;
    private Integer notifiedCount;
    private Double averageWaitTime;
    private Long timestamp;
}
