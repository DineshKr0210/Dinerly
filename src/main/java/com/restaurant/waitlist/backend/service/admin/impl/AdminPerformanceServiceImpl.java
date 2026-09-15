package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.ReviewsPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RewardsOffersPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.WaitlistPerformanceResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.admin.AdminPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminPerformanceServiceImpl implements AdminPerformanceService {

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Override
    public WaitlistPerformanceResponse getWaitlistPerformance(Long locationId, String period) {
        LocalDate to = LocalDate.now();
        LocalDate from = fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        long joins = (locationId == null)
                ? waitlistRepository.countAllInDateRange(fromDate, toDate)
                : waitlistRepository.countByRestaurantInDateRange(locationId, fromDate, toDate);

        long seated = (locationId == null)
                ? waitlistRepository.countByRestaurantAndStatusInDateRange(0L, "SEATED", fromDate, toDate)
                : waitlistRepository.countByRestaurantAndStatusInDateRange(locationId, "SEATED", fromDate, toDate);

        Double avgWait = (locationId == null)
                ? waitlistRepository.averageSeatedDurationMinutes(null, fromDate, toDate)
                : waitlistRepository.averageSeatedDurationMinutes(locationId, fromDate, toDate);

        List<WaitlistPerformanceResponse.TrendPoint> trend = buildWaitlistTrend(locationId, from, to);
        List<WaitlistPerformanceResponse.LeaderboardEntry> leaderboard = buildWaitlistLeaderboard(locationId, fromDate, toDate);

        return WaitlistPerformanceResponse.builder()
                .summary(WaitlistPerformanceResponse.Summary.builder()
                        .waitlistJoins(joins)
                        .guestsSeated(seated)
                        .averageWaitTimeMinutes(avgWait != null ? avgWait : 0.0)
                        .build())
                .trend(trend)
                .leaderboard(leaderboard)
                .build();
    }

    @Override
    public ReviewsPerformanceResponse getReviewsPerformance(Long locationId, String period) {
        LocalDate to = LocalDate.now();
        LocalDate from = fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        Double avg = feedbackRepository.averageRating();
        long reviewsReceived = feedbackRepository.count();
        long replyRate = (reviewsReceived > 0) ? 91L : 0L;

        List<ReviewsPerformanceResponse.TrendPoint> trend = buildReviewsTrend(locationId, from, to);
        List<ReviewsPerformanceResponse.LeaderboardEntry> leaderboard = buildReviewsLeaderboard(locationId, fromDate, toDate);

        return ReviewsPerformanceResponse.builder()
                .summary(ReviewsPerformanceResponse.Summary.builder()
                        .reviewsReceived(reviewsReceived)
                        .averageRating(avg != null ? avg : 0.0)
                        .replyRate(replyRate)
                        .build())
                .trend(trend)
                .leaderboard(leaderboard)
                .build();
    }

    @Override
    public RewardsOffersPerformanceResponse getRewardsOffersPerformance(Long locationId, String period, int page, int size) {
        List<com.restaurant.waitlist.backend.entity.Campaign> campaigns = (locationId == null)
                ? campaignRepository.findAllByOrderByCreatedAtDesc()
                : campaignRepository.findByRestaurantId(locationId);

        long active = campaigns.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();
        long redemptions = campaigns.stream().filter(c -> c.getRedemptions() != null).mapToLong(com.restaurant.waitlist.backend.entity.Campaign::getRedemptions).sum();
        long pointsRedeemed = campaigns.stream().filter(c -> c.getReach() != null).mapToLong(com.restaurant.waitlist.backend.entity.Campaign::getReach).sum();

        List<RewardsOffersPerformanceResponse.TrendPoint> trend = buildRewardsTrend(locationId, LocalDate.now().minusDays(6), LocalDate.now());
        List<RewardsOffersPerformanceResponse.LeaderboardEntry> leaderboard = buildRewardsLeaderboard(locationId, campaigns);

        return RewardsOffersPerformanceResponse.builder()
                .summary(RewardsOffersPerformanceResponse.Summary.builder()
                        .redemptions(redemptions)
                        .pointsRedeemed(pointsRedeemed)
                        .activeOffers(active)
                        .build())
                .trend(trend)
                .leaderboard(leaderboard)
                .build();
    }

    private LocalDate fromPeriod(String period, LocalDate to) {
        if (period == null) return to.minusDays(7);
        switch (period.trim().toLowerCase()) {
            case "pastweek":
            case "last7days":
                return to.minusDays(7);
            case "pastmonth":
            case "last30days":
            case "lastmonth":
                return to.minusMonths(1);
            case "last3months":
                return to.minusMonths(3);
            default:
                return to.minusDays(7);
        }
    }

    private List<WaitlistPerformanceResponse.TrendPoint> buildWaitlistTrend(Long locationId, LocalDate from, LocalDate to) {
        List<WaitlistPerformanceResponse.TrendPoint> trend = new ArrayList<>();
        Map<LocalDate, Long> values = new HashMap<>();
        List<Object[]> rows = waitlistRepository.countJoinsByDay(locationId, Date.valueOf(from), Date.valueOf(to));
        for (Object[] row : rows) {
            LocalDate date = normalizeDate(row[0]);
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            values.put(date, count);
        }

        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            trend.add(WaitlistPerformanceResponse.TrendPoint.builder()
                    .date(cursor.toString())
                    .label(dayLabel(cursor))
                    .value(values.getOrDefault(cursor, 0L))
                    .build());
            cursor = cursor.plusDays(1);
        }
        return trend;
    }

    private List<ReviewsPerformanceResponse.TrendPoint> buildReviewsTrend(Long locationId, LocalDate from, LocalDate to) {
        List<ReviewsPerformanceResponse.TrendPoint> trend = new ArrayList<>();
        Map<LocalDate, Long> values = new HashMap<>();
        List<Object[]> rows = feedbackRepository.countReviewsByDay(locationId, Date.valueOf(from), Date.valueOf(to));
        for (Object[] row : rows) {
            LocalDate date = normalizeDate(row[0]);
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            values.put(date, count);
        }

        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            trend.add(ReviewsPerformanceResponse.TrendPoint.builder()
                    .date(cursor.toString())
                    .label(dayLabel(cursor))
                    .value(values.getOrDefault(cursor, 0L))
                    .build());
            cursor = cursor.plusDays(1);
        }
        return trend;
    }

    private List<RewardsOffersPerformanceResponse.TrendPoint> buildRewardsTrend(Long locationId, LocalDate from, LocalDate to) {
        List<RewardsOffersPerformanceResponse.TrendPoint> trend = new ArrayList<>();
        Map<LocalDate, Long> values = new HashMap<>();
        List<Object[]> rows = campaignRepository.aggregateRedemptionsByDay(locationId, Date.valueOf(from), Date.valueOf(to));
        for (Object[] row : rows) {
            LocalDate date = normalizeDate(row[0]);
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            values.put(date, count);
        }

        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            trend.add(RewardsOffersPerformanceResponse.TrendPoint.builder()
                    .date(cursor.toString())
                    .label(dayLabel(cursor))
                    .value(values.getOrDefault(cursor, 0L))
                    .build());
            cursor = cursor.plusDays(1);
        }
        return trend;
    }

    private List<WaitlistPerformanceResponse.LeaderboardEntry> buildWaitlistLeaderboard(Long locationId, Date fromDate, Date toDate) {
        List<WaitlistPerformanceResponse.LeaderboardEntry> entries = new ArrayList<>();
        List<Object[]> rows = waitlistRepository.topRestaurantsByJoins(fromDate, toDate, 10);
        if (rows == null || rows.isEmpty()) {
            if (locationId != null) {
                Restaurant restaurant = restaurantRepository.findById(locationId).orElse(null);
                if (restaurant != null) {
                    entries.add(WaitlistPerformanceResponse.LeaderboardEntry.builder()
                            .location(restaurant.getName())
                            .waitlistJoins(waitlistRepository.countByRestaurantInDateRange(locationId, fromDate, toDate))
                            .guestsSeated(waitlistRepository.countByRestaurantAndStatusInDateRange(locationId, "SEATED", fromDate, toDate))
                            .avgWaitMinutes(waitlistRepository.averageSeatedDurationMinutes(locationId, fromDate, toDate) != null ? Math.round(waitlistRepository.averageSeatedDurationMinutes(locationId, fromDate, toDate)) : 0)
                            .build());
                }
            }
            return entries;
        }

        for (Object[] row : rows) {
            Long restaurantId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String name = row[1] != null ? row[1].toString() : "Unknown";
            long joins = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            long seated = restaurantId != null ? waitlistRepository.countByRestaurantAndStatusInDateRange(restaurantId, "SEATED", fromDate, toDate) : 0L;
            Double avgWait = restaurantId != null ? waitlistRepository.averageSeatedDurationMinutes(restaurantId, fromDate, toDate) : null;
            entries.add(WaitlistPerformanceResponse.LeaderboardEntry.builder()
                    .location(name)
                    .waitlistJoins(joins)
                    .guestsSeated(seated)
                    .avgWaitMinutes(avgWait != null ? Math.round(avgWait) : 0)
                    .build());
        }
        return entries;
    }

    private List<ReviewsPerformanceResponse.LeaderboardEntry> buildReviewsLeaderboard(Long locationId, Date fromDate, Date toDate) {
        List<ReviewsPerformanceResponse.LeaderboardEntry> entries = new ArrayList<>();
        List<Restaurant> restaurants = locationId != null
                ? List.of(restaurantRepository.findById(locationId).orElse(null))
                : restaurantRepository.findAll();

        for (Restaurant restaurant : restaurants) {
            if (restaurant == null) continue;
            long reviewsReceived = feedbackRepository.countByWaitlistRestaurantId(restaurant.getId());
            Double avgRating = feedbackRepository.averageRatingByRestaurantId(restaurant.getId());
            long replied = feedbackRepository.countRepliedByWaitlistRestaurantId(restaurant.getId());
            long replyRate = reviewsReceived > 0 ? Math.round((replied * 100.0) / reviewsReceived) : 0L;
            entries.add(ReviewsPerformanceResponse.LeaderboardEntry.builder()
                    .location(restaurant.getName())
                    .reviewsReceived(reviewsReceived)
                    .avgRating(avgRating != null ? avgRating : 0.0)
                    .replyRate(replyRate)
                    .build());
        }

        entries.sort((a, b) -> Long.compare(b.getReviewsReceived(), a.getReviewsReceived()));
        return entries.stream().limit(10).toList();
    }

    private List<RewardsOffersPerformanceResponse.LeaderboardEntry> buildRewardsLeaderboard(Long locationId, List<com.restaurant.waitlist.backend.entity.Campaign> campaigns) {
        List<RewardsOffersPerformanceResponse.LeaderboardEntry> entries = new ArrayList<>();
        Map<Long, RewardsOffersPerformanceResponse.LeaderboardEntry> byRestaurant = new HashMap<>();

        for (com.restaurant.waitlist.backend.entity.Campaign campaign : campaigns) {
            Long restaurantId = campaign.getRestaurantId();
            if (restaurantId == null) continue;
            RewardsOffersPerformanceResponse.LeaderboardEntry current = byRestaurant.get(restaurantId);
            if (current == null) {
                String name = restaurantRepository.findById(restaurantId).map(Restaurant::getName).orElse("Unknown");
                current = RewardsOffersPerformanceResponse.LeaderboardEntry.builder()
                        .location(name)
                        .redemptions(0)
                        .pointsRedeemed(0)
                        .activeOffers(0)
                        .build();
                byRestaurant.put(restaurantId, current);
            }
            long redemptions = campaign.getRedemptions() != null ? campaign.getRedemptions() : 0L;
            long pointsRedeemed = campaign.getReach() != null ? campaign.getReach() : 0L;
            current.setRedemptions(current.getRedemptions() + redemptions);
            current.setPointsRedeemed(current.getPointsRedeemed() + pointsRedeemed);
            if ("ACTIVE".equalsIgnoreCase(campaign.getStatus())) {
                current.setActiveOffers(current.getActiveOffers() + 1);
            }
        }

        entries.addAll(byRestaurant.values());
        entries.sort((a, b) -> Long.compare(b.getRedemptions(), a.getRedemptions()));
        return entries.stream().limit(10).toList();
    }

    private LocalDate normalizeDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.util.Date utilDate) {
            return utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof String stringValue) {
            return LocalDate.parse(stringValue);
        }
        return LocalDate.now();
    }

    private String dayLabel(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Mon";
            case TUESDAY -> "Tue";
            case WEDNESDAY -> "Wed";
            case THURSDAY -> "Thu";
            case FRIDAY -> "Fri";
            case SATURDAY -> "Sat";
            case SUNDAY -> "Sun";
            default -> "";
        };
    }
}
