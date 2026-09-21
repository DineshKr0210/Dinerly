package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.AdminDashboardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.InsightCardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RealTimeMetricsResponse;
import java.time.LocalDate;
import java.util.List;

public interface AdminDashboardService {
    AdminDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, int topN, Long locationId);

    String getCurrentUserName();
    
    // Real-time metrics
    RealTimeMetricsResponse getRealTimeMetrics(Long locationId);
    
    // Insights and actions feed
    List<InsightCardResponse> getInsightsFeed(String category, Long locationId);
    
    // Quick actions
    List<java.util.Map<String, String>> getQuickActions();
}

