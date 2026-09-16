package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.AdminDashboardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.InsightCardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.LocationLeaderboardItem;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final RestaurantRepository restaurantRepository;
    private final WaitlistRepository waitlistRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    public AdminDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, int topN, Long locationId) {
        Date from = fromDate != null ? Date.valueOf(fromDate) : null;
        Date to = toDate != null ? Date.valueOf(toDate) : null;

        long totalRestaurants = locationId != null ? 1 : restaurantRepository.count();

        long totalWaitlistJoins = (locationId != null)
                ? waitlistRepository.countByRestaurantIdInDateRange(locationId, from, to)
                : waitlistRepository.countAllInDateRange(from, to);

        List<Waitlist> waiting = locationId != null
                ? waitlistRepository.findByRestaurantIdAndStatus(locationId, Waitlist.WaitlistStatus.WAITING)
                : waitlistRepository.findByStatus(Waitlist.WaitlistStatus.WAITING);
        List<Waitlist> notified = locationId != null
                ? waitlistRepository.findByRestaurantIdAndStatus(locationId, Waitlist.WaitlistStatus.NOTIFIED)
                : waitlistRepository.findByStatus(Waitlist.WaitlistStatus.NOTIFIED);
        long totalActive = (waiting != null ? waiting.size() : 0) + (notified != null ? notified.size() : 0);

        Double avgRating = locationId != null ? feedbackRepository.averageRatingByRestaurantId(locationId) : feedbackRepository.averageRating();
        if (avgRating == null) avgRating = 0.0;

        List<Object[]> top = (locationId != null)
                ? waitlistRepository.topRestaurantByJoinsForLocation(locationId, from, to, topN)
                : waitlistRepository.topRestaurantsByJoins(from, to, topN);
        List<LocationLeaderboardItem> leaderboard = new ArrayList<>();
        if (top != null) {
            for (Object[] row : top) {
                Long restaurantId = row[0] != null ? ((Number) row[0]).longValue() : null;
                String name = row[1] != null ? row[1].toString() : null;
                Long joins = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                leaderboard.add(LocationLeaderboardItem.builder()
                        .restaurantId(restaurantId)
                        .name(name)
                        .joins(joins)
                        .build());
            }
        }

        return AdminDashboardResponse.builder()
                .totalRestaurants(totalRestaurants)
                .totalWaitlistJoins(totalWaitlistJoins)
                .totalActiveWaitlists(totalActive)
                .averageRating(avgRating)
                .topLocations(leaderboard)
                .build();
    }

    @Override
    public Map<String, Object> getRealTimeMetrics(Long locationId) {
        Map<String, Object> metrics = new HashMap<>();
        
        List<Waitlist> waiting = locationId != null
                ? waitlistRepository.findByRestaurantIdAndStatus(locationId, Waitlist.WaitlistStatus.WAITING)
                : waitlistRepository.findByStatus(Waitlist.WaitlistStatus.WAITING);
        
        List<Waitlist> notified = locationId != null
                ? waitlistRepository.findByRestaurantIdAndStatus(locationId, Waitlist.WaitlistStatus.NOTIFIED)
                : waitlistRepository.findByStatus(Waitlist.WaitlistStatus.NOTIFIED);
        
        metrics.put("activeWaitlists", (waiting != null ? waiting.size() : 0) + (notified != null ? notified.size() : 0));
        metrics.put("waitingCount", waiting != null ? waiting.size() : 0);
        metrics.put("notifiedCount", notified != null ? notified.size() : 0);
        metrics.put("averageWaitTime", waiting != null && waiting.size() > 0 
            ? waiting.stream().mapToInt(w -> w.getEstimatedWaitTime() != null ? w.getEstimatedWaitTime() : 0).average().orElse(0) 
            : 0);
        metrics.put("timestamp", System.currentTimeMillis());
        
        return metrics;
    }

    @Override
    public Page<InsightCardResponse> getInsightsFeed(String category, Long locationId, Pageable pageable) {
        List<InsightCardResponse> insights = new ArrayList<>();
        
        // GROWTH insights
        if (category == null || "Growth".equalsIgnoreCase(category) || "All".equalsIgnoreCase(category)) {
            long activeOffers = restaurantRepository.count(); // Simplified
            if (activeOffers == 0) {
                insights.add(InsightCardResponse.builder()
                    .type("GROWTH")
                    .title("No Active Offers")
                    .description("Locations without active offers")
                    .message("2 locations are leaving waitlist joins on the table — consider an offer")
                    .actionLabel("Create offer")
                    .actionUrl("/offers")
                    .priority("HIGH")
                    .metric("2 locations")
                    .timestamp(System.currentTimeMillis())
                    .build());
            }
        }
        
        // OPERATIONS insights
        if (category == null || "Operations".equalsIgnoreCase(category) || "All".equalsIgnoreCase(category)) {
            List<Waitlist> waiting = waitlistRepository.findByStatus(Waitlist.WaitlistStatus.WAITING);
            if (waiting != null && waiting.size() > 10) {
                insights.add(InsightCardResponse.builder()
                    .type("OPERATIONS")
                    .title("No-shows are quietly costing you tables")
                    .description("Locations with high no-show rates")
                    .message("St. Brendan's is seeing 18% no-show rate")
                    .actionLabel("Review hold-time settings")
                    .actionUrl("/locations/settings")
                    .priority("MEDIUM")
                    .metric("18%")
                    .timestamp(System.currentTimeMillis())
                    .build());
            }
        }
        
        // TIPS insights
        if (category == null || "Tips".equalsIgnoreCase(category) || "All".equalsIgnoreCase(category)) {
            Double avgRating = feedbackRepository.averageRating();
            if (avgRating != null && avgRating < 4.0) {
                insights.add(InsightCardResponse.builder()
                    .type("TIPS")
                    .title("Monitor wait time accuracy")
                    .description("Wait time accuracy flags")
                    .message("Estimated vs actual times drifting - guests expect 30 min but wait 40")
                    .actionLabel("Review wait estimates")
                    .actionUrl("/performance")
                    .priority("MEDIUM")
                    .metric("10 min drift")
                    .timestamp(System.currentTimeMillis())
                    .build());
            }
        }
        
        // ANNOUNCEMENTS insights
        if (category == null || "Announcements".equalsIgnoreCase(category) || "All".equalsIgnoreCase(category)) {
            insights.add(InsightCardResponse.builder()
                .type("ANNOUNCEMENTS")
                .title("New Rewards Program Available")
                .description("Product update")
                .message("Configure new loyalty tiers: Silver, Gold, Platinum")
                .actionLabel("View Rewards")
                .actionUrl("/rewards-program")
                .priority("LOW")
                .timestamp(System.currentTimeMillis())
                .build());
        }
        
        return new PageImpl<>(insights, pageable, insights.size());
    }

    @Override
    public List<Map<String, String>> getQuickActions() {
        List<Map<String, String>> actions = new ArrayList<>();
        
        actions.add(Map.of(
            "label", "Create offer",
            "icon", "🎁",
            "url", "/offers"
        ));
        
        actions.add(Map.of(
            "label", "Edit menu item",
            "icon", "📋",
            "url", "/menu-management"
        ));
        
        actions.add(Map.of(
            "label", "Edit store hours",
            "icon", "🕐",
            "url", "/store-availability"
        ));
        
        actions.add(Map.of(
            "label", "Add staff member",
            "icon", "👤",
            "url", "/admin"
        ));
        
        actions.add(Map.of(
            "label", "Generate report",
            "icon", "📊",
            "url", "/reports"
        ));
        
        actions.add(Map.of(
            "label", "Help center",
            "icon", "❓",
            "url", "/help"
        ));
        
        return actions;
    }
}

