package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardsOffersPerformanceResponse {
    private long activeCampaigns;
    private long guestsReached;
    private long redemptions;
    private BigDecimal revenueInfluenced;
    private List<CampaignRow> campaigns;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignRow {
        private Long id;
        private String name;
        private String channel;
        private String audience;
        private Integer reach;
        private Integer redemptions;
        private String status;
        private java.time.LocalDateTime scheduledAt;
    }
}
