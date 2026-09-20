package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.AdminDashboardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.InsightCardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.LocationLeaderboardItem;
import com.restaurant.waitlist.backend.dto.response.admin.RealTimeMetricsResponse;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.OfferRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
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
    private final OfferRepository offerRepository;

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
    public RealTimeMetricsResponse getRealTimeMetrics(Long locationId) {
        List<Waitlist> waiting = locationId != null
                ? waitlistRepository.findByRestaurantIdAndStatus(locationId, Waitlist.WaitlistStatus.WAITING)
                : waitlistRepository.findByStatus(Waitlist.WaitlistStatus.WAITING);
        
        List<Waitlist> notified = locationId != null
                ? waitlistRepository.findByRestaurantIdAndStatus(locationId, Waitlist.WaitlistStatus.NOTIFIED)
                : waitlistRepository.findByStatus(Waitlist.WaitlistStatus.NOTIFIED);
        
        int waitingCount = waiting != null ? waiting.size() : 0;
        int notifiedCount = notified != null ? notified.size() : 0;
        int activeWaitlists = waitingCount + notifiedCount;
        
        double averageWaitTime = waiting != null && waiting.size() > 0 
            ? waiting.stream().mapToInt(w -> w.getEstimatedWaitTime() != null ? w.getEstimatedWaitTime() : 0).average().orElse(0) 
            : 0;
        
        return RealTimeMetricsResponse.builder()
                .activeWaitlists(activeWaitlists)
                .waitingCount(waitingCount)
                .notifiedCount(notifiedCount)
                .averageWaitTime(averageWaitTime)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Override
    public List<InsightCardResponse> getInsightsFeed(String category, Long locationId) {
        List<InsightCardResponse> insights = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        Date from = Date.valueOf(startDate);
        Date to = Date.valueOf(endDate);

        // GROWTH insights - Locations without active offers
        if (category == null || "GROWTH".equalsIgnoreCase(category) || "All".equalsIgnoreCase(category)) {
            insights.addAll(generateGrowthInsights(locationId, from, to));
        }

        // OPERATIONS insights - No-shows & Closed locations
        if (category == null || "OPERATIONS".equalsIgnoreCase(category) || "All".equalsIgnoreCase(category)) {
            insights.addAll(generateOperationsInsights(locationId, from, to));
        }

        // TIPS insights - Wait time accuracy
        if (category == null || "TIPS".equalsIgnoreCase(category) || "All".equalsIgnoreCase(category)) {
            insights.addAll(generateTipsInsights(locationId, from, to));
        }

        // ANNOUNCEMENTS insights
        if (category == null || "ANNOUNCEMENTS".equalsIgnoreCase(category) || "All".equalsIgnoreCase(category)) {
            insights.addAll(generateAnnouncementInsights());
        }

        return insights;
    }

    private List<InsightCardResponse> generateGrowthInsights(Long locationId, Date from, Date to) {
        List<InsightCardResponse> insights = new ArrayList<>();

        if (locationId != null) {
            // Single location - check if it has active offers
            long activeOffers = offerRepository.countActiveOffersByRestaurant(locationId);
            long totalWaitlistJoins = waitlistRepository.countByRestaurantIdInDateRange(locationId, from, to);
            
            if (activeOffers == 0 && totalWaitlistJoins > 0) {
                insights.add(InsightCardResponse.builder()
                    .type("GROWTH")
                    .title("Missing waitlist join opportunity")
                    .description("This location has no active offers")
                    .message("Consider creating an offer to boost waitlist joins — locations with active offers see higher engagement")
                    .actionLabel("Create offer")
                    .actionUrl("/offers?restaurantId=" + locationId)
                    .priority("HIGH")
                    .metric("0 active offers")
                    .timestamp(System.currentTimeMillis())
                    .build());
            } else if (activeOffers > 0 && totalWaitlistJoins > 0) {
                // Location has offers - show positive insight
                insights.add(InsightCardResponse.builder()
                    .type("GROWTH")
                    .title("Active offers running")
                    .description("Offers boosting engagement")
                    .message("Location has " + activeOffers + " active offer(s). Total waitlist joins this month: " + totalWaitlistJoins)
                    .actionLabel("View offers")
                    .actionUrl("/offers?restaurantId=" + locationId)
                    .priority("LOW")
                    .metric(activeOffers + " active offers")
                    .timestamp(System.currentTimeMillis())
                    .build());
            }
        } else {
            // All locations - find those without active offers
            List<Object[]> locationOfferStats = waitlistRepository.topRestaurantsByJoins(from, to, 100);
            List<Long> restaurantIds = new ArrayList<>();
            
            for (Object[] row : locationOfferStats) {
                Long restaurantId = row[0] != null ? ((Number) row[0]).longValue() : null;
                if (restaurantId != null) {
                    restaurantIds.add(restaurantId);
                }
            }

            // Check each location for active offers
            List<Long> locationsWithoutOffers = new ArrayList<>();
            for (Long rId : restaurantIds) {
                long activeOffers = offerRepository.countActiveOffersByRestaurant(rId);
                if (activeOffers == 0) {
                    locationsWithoutOffers.add(rId);
                }
            }

            if (!locationsWithoutOffers.isEmpty()) {
                insights.add(InsightCardResponse.builder()
                    .type("GROWTH")
                    .title("Locations without active offers")
                    .description("Leave money on the table")
                    .message(locationsWithoutOffers.size() + " locations are missing out on offer-driven waitlist boosts — active offers increase engagement by an average of 25%")
                    .actionLabel("Manage offers")
                    .actionUrl("/offers")
                    .priority("HIGH")
                    .metric(locationsWithoutOffers.size() + " locations")
                    .timestamp(System.currentTimeMillis())
                    .build());
            }
        }

        return insights;
    }

    private List<InsightCardResponse> generateOperationsInsights(Long locationId, Date from, Date to) {
        List<InsightCardResponse> insights = new ArrayList<>();

        if (locationId != null) {
            // Single location - No-show rate
            Double noShowRate = waitlistRepository.getNoShowRateByLocation(locationId, from, to);
            if (noShowRate != null && noShowRate > 15.0) {
                insights.add(InsightCardResponse.builder()
                    .type("OPERATIONS")
                    .title("Elevated no-show rate detected")
                    .description("No-shows are costing you table availability")
                    .message("This location has a " + String.format("%.1f", noShowRate) + "% no-show rate — consider adjusting hold times or improving reminder logic")
                    .actionLabel("Review hold-time settings")
                    .actionUrl("/locations/" + locationId + "/settings")
                    .priority("HIGH")
                    .metric(String.format("%.1f", noShowRate) + "%")
                    .timestamp(System.currentTimeMillis())
                    .build());
            }

            // Closed location check
            restaurantRepository.findClosedLocationById(locationId).ifPresent(r -> {
                insights.add(InsightCardResponse.builder()
                    .type("OPERATIONS")
                    .title("Location is closed")
                    .description("Closed with no scheduled reopening")
                    .message("'" + r.getName() + "' is currently closed — schedule a reopen time to resume accepting waitlist entries")
                    .actionLabel("Schedule reopening")
                    .actionUrl("/store-availability")
                    .priority("HIGH")
                    .metric("Closed")
                    .timestamp(System.currentTimeMillis())
                    .build());
            });
        } else {
            // All locations - Find high no-show rate locations
            List<Object[]> topLocations = waitlistRepository.topRestaurantsByJoins(from, to, 50);
            for (Object[] row : topLocations) {
                Long rId = row[0] != null ? ((Number) row[0]).longValue() : null;
                String name = row[1] != null ? row[1].toString() : null;
                
                if (rId != null) {
                    Double noShowRate = waitlistRepository.getNoShowRateByLocation(rId, from, to);
                    if (noShowRate != null && noShowRate > 15.0) {
                        insights.add(InsightCardResponse.builder()
                            .type("OPERATIONS")
                            .title("High no-show rate at " + name)
                            .description("Operational efficiency issue")
                            .message(name + " has a " + String.format("%.1f", noShowRate) + "% no-show rate — this impacts table utilization and guest satisfaction")
                            .actionLabel("Review hold-time settings")
                            .actionUrl("/store-availability")
                            .priority("MEDIUM")
                            .metric(String.format("%.1f", noShowRate) + "%")
                            .timestamp(System.currentTimeMillis())
                            .build());
                        // Limit to top 2 no-show insights
                        if (insights.stream().filter(i -> i.getType().equals("OPERATIONS")).count() >= 2) break;
                    }
                }
            }

            // Closed locations without reopen schedule
            List<Object[]> closedLocations = restaurantRepository.findClosedLocations().stream()
                    .map(r -> new Object[]{r.getId(), r.getName()})
                    .collect(Collectors.toList());
            
            for (Object[] row : closedLocations) {
                Long rId = (Long) row[0];
                String name = (String) row[1];
                insights.add(InsightCardResponse.builder()
                    .type("OPERATIONS")
                    .title(name + " is closed")
                    .description("Closed with no reopening schedule")
                    .message(name + " is currently closed — schedule a reopen time to resume accepting waitlist entries")
                    .actionLabel("Schedule reopening")
                    .actionUrl("/store-availability")
                    .priority("HIGH")
                    .metric("Closed")
                    .timestamp(System.currentTimeMillis())
                    .build());
            }
        }

        return insights;
    }

    private List<InsightCardResponse> generateTipsInsights(Long locationId, Date from, Date to) {
        List<InsightCardResponse> insights = new ArrayList<>();

        if (locationId != null) {
            // Single location - Wait time accuracy
            Double waitTimeVariance = waitlistRepository.getWaitTimeAccuracyByLocation(locationId, from, to);
            if (waitTimeVariance != null && Math.abs(waitTimeVariance) > 5.0) {
                String direction = waitTimeVariance > 0 ? "longer" : "shorter";
                insights.add(InsightCardResponse.builder()
                    .type("TIPS")
                    .title("Wait time estimates need adjustment")
                    .description("Estimated vs actual times are drifting")
                    .message("Guests at this location are waiting " + Math.abs(Math.round(waitTimeVariance)) + " minutes " + direction + " than estimated — consider fine-tuning wait time forecasts")
                    .actionLabel("View performance metrics")
                    .actionUrl("/performance?tab=waitlist")
                    .priority("MEDIUM")
                    .metric(String.format("%.1f", Math.abs(waitTimeVariance)) + " min variance")
                    .timestamp(System.currentTimeMillis())
                    .build());
            }
        } else {
            // All locations - Find those with significant wait time variance
            List<Object[]> topLocations = waitlistRepository.topRestaurantsByJoins(from, to, 30);
            for (Object[] row : topLocations) {
                Long rId = row[0] != null ? ((Number) row[0]).longValue() : null;
                String name = row[1] != null ? row[1].toString() : null;
                
                if (rId != null) {
                    Double waitTimeVariance = waitlistRepository.getWaitTimeAccuracyByLocation(rId, from, to);
                    if (waitTimeVariance != null && Math.abs(waitTimeVariance) > 5.0) {
                        String direction = waitTimeVariance > 0 ? "longer" : "shorter";
                        insights.add(InsightCardResponse.builder()
                            .type("TIPS")
                            .title("Wait time accuracy issue at " + name)
                            .description("Accuracy opportunity")
                            .message("Guests at " + name + " are waiting " + Math.abs(Math.round(waitTimeVariance)) + " minutes " + direction + " than estimated")
                            .actionLabel("Review metrics")
                            .actionUrl("/performance?tab=waitlist")
                            .priority("LOW")
                            .metric(String.format("%.1f", Math.abs(waitTimeVariance)) + " min")
                            .timestamp(System.currentTimeMillis())
                            .build());
                        if (insights.stream().filter(i -> i.getType().equals("TIPS")).count() >= 2) break;
                    }
                }
            }
        }

        return insights;
    }

    private List<InsightCardResponse> generateAnnouncementInsights() {
        List<InsightCardResponse> insights = new ArrayList<>();

        // Product announcements - could be pulled from a database table
        // For now, include general system announcements
        insights.add(InsightCardResponse.builder()
            .type("ANNOUNCEMENTS")
            .title("Rewards Program Active")
            .description("Feature announcement")
            .message("Your Rewards & Loyalty program is live — manage tiers, redemptions, and earning rules to boost customer retention")
            .actionLabel("Manage Rewards")
            .actionUrl("/rewards")
            .priority("LOW")
            .timestamp(System.currentTimeMillis())
            .build());

        return insights;
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

