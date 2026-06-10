package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private Long totalWaiting;
    private Long totalNotified;
    private Integer averageWaitTime;
    private Long seatedToday;
    private Long noShowsToday;
    private Long openTables;
    private Long occupiedTables;
    private Long reservedTables;
    private Long tablesNeedingCleaning;
}

