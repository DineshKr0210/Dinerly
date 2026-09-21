package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.AdminDashboardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.InsightCardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RealTimeMetricsResponse;
import com.restaurant.waitlist.backend.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "5") int topN,
            @RequestParam(required = false) Long locationId) {
        try {
            LocalDate fromDate = from != null ? LocalDate.parse(from) : null;
            LocalDate toDate = to != null ? LocalDate.parse(to) : null;
            AdminDashboardResponse resp = adminDashboardService.getDashboard(fromDate, toDate, topN, locationId);
            resp.setUserName(adminDashboardService.getCurrentUserName());
            return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved", resp));
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid date format"));
        }
    }

    @GetMapping("/realtime-metrics")
    public ResponseEntity<ApiResponse<RealTimeMetricsResponse>> getRealTimeMetrics(
            @RequestParam(required = false) Long locationId) {
        RealTimeMetricsResponse metrics = adminDashboardService.getRealTimeMetrics(locationId);
        return ResponseEntity.ok(ApiResponse.success("Real-time metrics retrieved", metrics));
    }

    @GetMapping("/insights-feed")
    public ResponseEntity<ApiResponse<List<InsightCardResponse>>> getInsightsFeed(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long locationId) {
        List<InsightCardResponse> insights = adminDashboardService.getInsightsFeed(category, locationId);
        return ResponseEntity.ok(ApiResponse.success("Insights feed retrieved", insights));
    }

    @GetMapping("/quick-actions")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getQuickActions() {
        List<Map<String, String>> actions = adminDashboardService.getQuickActions();
        return ResponseEntity.ok(ApiResponse.success("Quick actions retrieved", actions));
    }
}
