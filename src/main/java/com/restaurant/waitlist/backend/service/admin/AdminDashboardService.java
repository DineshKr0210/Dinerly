package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.AdminDashboardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.InsightCardResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminDashboardService {
    AdminDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, int topN, Long locationId);
    
    // Real-time metrics
    java.util.Map<String, Object> getRealTimeMetrics(Long locationId);
    
    // Insights and actions feed
    Page<InsightCardResponse> getInsightsFeed(String category, Long locationId, Pageable pageable);
    
    // Quick actions
    List<java.util.Map<String, String>> getQuickActions();
}

