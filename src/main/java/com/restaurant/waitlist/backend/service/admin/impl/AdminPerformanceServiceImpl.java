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
    public WaitlistPerformanceResponse getWaitlistPerformance(Long locationId, String period, LocalDate startDate, LocalDate endDate) {
        // Use custom dates if provided, otherwise use period-based dates
        LocalDate to = endDate != null ? endDate : LocalDate.now();
        LocalDate from = startDate != null ? startDate : fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        // Current period metrics
        long joins = (locationId == null)
                ? waitlistRepository.countAllInDateRange(fromDate, toDate)
                : waitlistRepository.countByRestaurantInDateRange(locationId, fromDate, toDate);

        long seated = (locationId == null)
                ? waitlistRepository.countByRestaurantAndStatusInDateRange(null, "SEATED", fromDate, toDate)
                : waitlistRepository.countByRestaurantAndStatusInDateRange(locationId, "SEATED", fromDate, toDate);

        Double avgWait = (locationId == null)
                ? waitlistRepository.averageSeatedDurationMinutes(null, fromDate, toDate)
                : waitlistRepository.averageSeatedDurationMinutes(locationId, fromDate, toDate);

        // Previous period metrics for comparison
        LocalDate prevFrom = getPreviousPeriodStart(period, from, startDate);
        LocalDate prevTo = from.minusDays(1);
        Date prevFromDate = Date.valueOf(prevFrom);
        Date prevToDate = Date.valueOf(prevTo);

        long prevJoins = (locationId == null)
                ? waitlistRepository.countAllInDateRange(prevFromDate, prevToDate)
                : waitlistRepository.countByRestaurantInDateRange(locationId, prevFromDate, prevToDate);

        long prevSeated = (locationId == null)
                ? waitlistRepository.countByRestaurantAndStatusInDateRange(null, "SEATED", prevFromDate, prevToDate)
                : waitlistRepository.countByRestaurantAndStatusInDateRange(locationId, "SEATED", prevFromDate, prevToDate);

        Double prevAvgWait = (locationId == null)
                ? waitlistRepository.averageSeatedDurationMinutes(null, prevFromDate, prevToDate)
                : waitlistRepository.averageSeatedDurationMinutes(locationId, prevFromDate, prevToDate);

        List<WaitlistPerformanceResponse.TrendPoint> trend = buildWaitlistTrend(locationId, from, to);
        List<WaitlistPerformanceResponse.LeaderboardEntry> leaderboard = buildWaitlistLeaderboard(locationId, fromDate, toDate);

        return WaitlistPerformanceResponse.builder()
                .summary(WaitlistPerformanceResponse.Summary.builder()
                        .waitlistJoins(joins)
                        .prevWaitlistJoins(prevJoins)
                        .waitlistJoinsChangePercent(calculateChangePercent(prevJoins, joins))
                        .guestsSeated(seated)
                        .prevGuestsSeated(prevSeated)
                        .guestsSeatedChangePercent(calculateChangePercent(prevSeated, seated))
                        .averageWaitTimeMinutes(avgWait != null ? avgWait : 0.0)
                        .prevAverageWaitTimeMinutes(prevAvgWait != null ? prevAvgWait : 0.0)
                        .avgWaitChangePercent(calculateChangePercent(prevAvgWait != null ? prevAvgWait.longValue() : 0L, avgWait != null ? avgWait.longValue() : 0L))
                        .build())
                .trend(trend)
                .leaderboard(leaderboard)
                .build();
    }

    @Override
    public ReviewsPerformanceResponse getReviewsPerformance(Long locationId, String period, LocalDate startDate, LocalDate endDate) {
        // Use custom dates if provided, otherwise use period-based dates
        LocalDate to = endDate != null ? endDate : LocalDate.now();
        LocalDate from = startDate != null ? startDate : fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        // Current period reviews
        long reviewsReceived = (locationId == null)
                ? feedbackRepository.countByDateRange(fromDate, toDate)
                : feedbackRepository.countByWaitlistRestaurantIdAndDateRange(locationId, fromDate, toDate);

        Double avgRating = (locationId == null)
                ? feedbackRepository.averageRatingByDateRange(fromDate, toDate)
                : feedbackRepository.averageRatingByRestaurantIdAndDateRange(locationId, fromDate, toDate);

        long replied = (locationId == null)
                ? feedbackRepository.countRepliedByDateRange(fromDate, toDate)
                : feedbackRepository.countRepliedByRestaurantIdAndDateRange(locationId, fromDate, toDate);

        long replyRate = reviewsReceived > 0 ? Math.round((replied * 100.0) / reviewsReceived) : 0L;

        // Previous period reviews
        LocalDate prevFrom = getPreviousPeriodStart(period, from, startDate);
        LocalDate prevTo = from.minusDays(1);
        Date prevFromDate = Date.valueOf(prevFrom);
        Date prevToDate = Date.valueOf(prevTo);

        long prevReviewsReceived = (locationId == null)
                ? feedbackRepository.countByDateRange(prevFromDate, prevToDate)
                : feedbackRepository.countByWaitlistRestaurantIdAndDateRange(locationId, prevFromDate, prevToDate);

        Double prevAvgRating = (locationId == null)
                ? feedbackRepository.averageRatingByDateRange(prevFromDate, prevToDate)
                : feedbackRepository.averageRatingByRestaurantIdAndDateRange(locationId, prevFromDate, prevToDate);

        long prevReplied = (locationId == null)
                ? feedbackRepository.countRepliedByDateRange(prevFromDate, prevToDate)
                : feedbackRepository.countRepliedByRestaurantIdAndDateRange(locationId, prevFromDate, prevToDate);

        long prevReplyRate = prevReviewsReceived > 0 ? Math.round((prevReplied * 100.0) / prevReviewsReceived) : 0L;

        List<ReviewsPerformanceResponse.TrendPoint> trend = buildReviewsTrend(locationId, from, to);
        List<ReviewsPerformanceResponse.LeaderboardEntry> leaderboard = buildReviewsLeaderboard(locationId, fromDate, toDate);

        return ReviewsPerformanceResponse.builder()
                .summary(ReviewsPerformanceResponse.Summary.builder()
                        .reviewsReceived(reviewsReceived)
                        .prevReviewsReceived(prevReviewsReceived)
                        .reviewsReceivedChangePercent(calculateChangePercent(prevReviewsReceived, reviewsReceived))
                        .averageRating(avgRating != null ? avgRating : 0.0)
                        .prevAverageRating(prevAvgRating != null ? prevAvgRating : 0.0)
                        .avgRatingChangePercent(calculateChangePercent(prevAvgRating != null ? prevAvgRating.longValue() : 0L, avgRating != null ? avgRating.longValue() : 0L))
                        .replyRate(replyRate)
                        .prevReplyRate(prevReplyRate)
                        .replyRateChangePercent(calculateChangePercent(prevReplyRate, replyRate))
                        .build())
                .trend(trend)
                .leaderboard(leaderboard)
                .build();
    }

    @Override
    public RewardsOffersPerformanceResponse getRewardsOffersPerformance(Long locationId, String period, LocalDate startDate, LocalDate endDate, int page, int size) {
        // Use custom dates if provided, otherwise use period-based dates
        LocalDate to = endDate != null ? endDate : LocalDate.now();
        LocalDate from = startDate != null ? startDate : fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        // Current period campaigns/redemptions
        List<com.restaurant.waitlist.backend.entity.Campaign> campaigns = (locationId == null)
                ? campaignRepository.findAllByOrderByCreatedAtDesc()
                : campaignRepository.findByRestaurantId(locationId);

        long activeOffers = campaigns.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();
        long redemptions = campaigns.stream()
                .filter(c -> c.getRedemptions() != null)
                .mapToLong(com.restaurant.waitlist.backend.entity.Campaign::getRedemptions)
                .sum();
        long pointsIssued = campaigns.stream()
                .filter(c -> c.getReach() != null)
                .mapToLong(com.restaurant.waitlist.backend.entity.Campaign::getReach)
                .sum();

        // Previous period campaigns/redemptions
        LocalDate prevFrom = getPreviousPeriodStart(period, from, startDate);
        LocalDate prevTo = from.minusDays(1);
        Date prevFromDate = Date.valueOf(prevFrom);
        Date prevToDate = Date.valueOf(prevTo);

        List<com.restaurant.waitlist.backend.entity.Campaign> prevCampaigns = (locationId == null)
                ? campaignRepository.findAllByOrderByCreatedAtDesc()
                : campaignRepository.findByRestaurantId(locationId);

        long prevActiveOffers = prevCampaigns.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();
        long prevRedemptions = prevCampaigns.stream()
                .filter(c -> c.getRedemptions() != null)
                .mapToLong(com.restaurant.waitlist.backend.entity.Campaign::getRedemptions)
                .sum();
        long prevPointsIssued = prevCampaigns.stream()
                .filter(c -> c.getReach() != null)
                .mapToLong(com.restaurant.waitlist.backend.entity.Campaign::getReach)
                .sum();

        List<RewardsOffersPerformanceResponse.TrendPoint> trend = buildRewardsTrend(locationId, from, to);
        List<RewardsOffersPerformanceResponse.LeaderboardEntry> leaderboard = buildRewardsLeaderboard(locationId, campaigns);

        return RewardsOffersPerformanceResponse.builder()
                .summary(RewardsOffersPerformanceResponse.Summary.builder()
                        .redemptions(redemptions)
                        .prevRedemptions(prevRedemptions)
                        .redemptionsChangePercent(calculateChangePercent(prevRedemptions, redemptions))
                        .pointsIssued(pointsIssued)
                        .prevPointsIssued(prevPointsIssued)
                        .pointsIssuedChangePercent(calculateChangePercent(prevPointsIssued, pointsIssued))
                        .activeOffers(activeOffers)
                        .prevActiveOffers(prevActiveOffers)
                        .activeOffersChangePercent(calculateChangePercent(prevActiveOffers, activeOffers))
                        .build())
                .trend(trend)
                .leaderboard(leaderboard)
                .build();
    }

    /**
     * Calculate percent change from previous to current value.
     * Returns positive for increase, negative for decrease.
     */
    private double calculateChangePercent(long prevValue, long currentValue) {
        if (prevValue == 0) {
            return currentValue > 0 ? 100.0 : 0.0;
        }
        return ((currentValue - prevValue) / (double) prevValue) * 100.0;
    }

    /**
     * Get the start date of the previous period for comparison.
     * If custom startDate is provided, calculates previous period based on same duration.
     */
    private LocalDate getPreviousPeriodStart(String period, LocalDate currentPeriodStart, LocalDate customStartDate) {
        // If custom start date provided, use same period length for previous period
        if (customStartDate != null) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(customStartDate, currentPeriodStart);
            return customStartDate.minusDays(daysBetween + 1);
        }
        
        // Otherwise use period string
        if (period == null) return currentPeriodStart.minusDays(7);
        switch (period.trim().toLowerCase()) {
            case "pastweek":
            case "last7days":
                return currentPeriodStart.minusDays(7);
            case "pastmonth":
            case "last30days":
            case "lastmonth":
                return currentPeriodStart.minusMonths(1);
            case "last3months":
                return currentPeriodStart.minusMonths(3);
            default:
                return currentPeriodStart.minusDays(7);
        }
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
                    Double rating = feedbackRepository.averageRatingByRestaurantId(locationId);
                    long redemptions = 0; // Get from campaigns/redemptions data
                    entries.add(WaitlistPerformanceResponse.LeaderboardEntry.builder()
                            .rank(1)
                            .location(restaurant.getName())
                            .waitlistJoins(waitlistRepository.countByRestaurantInDateRange(locationId, fromDate, toDate))
                            .redemptions(redemptions)
                            .avgWaitMinutes(waitlistRepository.averageSeatedDurationMinutes(locationId, fromDate, toDate) != null ? Math.round(waitlistRepository.averageSeatedDurationMinutes(locationId, fromDate, toDate)) : 0)
                            .rating(rating != null ? rating : 0.0)
                            .build());
                }
            }
            return entries;
        }

        int rank = 1;
        for (Object[] row : rows) {
            Long restaurantId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String name = row[1] != null ? row[1].toString() : "Unknown";
            long joins = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Double avgWait = restaurantId != null ? waitlistRepository.averageSeatedDurationMinutes(restaurantId, fromDate, toDate) : null;
            Double rating = restaurantId != null ? feedbackRepository.averageRatingByRestaurantId(restaurantId) : null;
            long redemptions = restaurantId != null ? (campaignRepository.findByRestaurantId(restaurantId).stream()
                    .filter(c -> c.getRedemptions() != null)
                    .mapToLong(com.restaurant.waitlist.backend.entity.Campaign::getRedemptions)
                    .sum()) : 0L;
            
            entries.add(WaitlistPerformanceResponse.LeaderboardEntry.builder()
                    .rank(rank++)
                    .location(name)
                    .waitlistJoins(joins)
                    .redemptions(redemptions)
                    .avgWaitMinutes(avgWait != null ? Math.round(avgWait) : 0)
                    .rating(rating != null ? rating : 0.0)
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
        List<ReviewsPerformanceResponse.LeaderboardEntry> rankedEntries = new ArrayList<>();
        int rank = 1;
        for (ReviewsPerformanceResponse.LeaderboardEntry entry : entries.stream().limit(10).toList()) {
            entry.setRank(rank++);
            rankedEntries.add(entry);
        }
        return rankedEntries;
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
                        .pointsIssued(0)
                        .activeOffers(0)
                        .build();
                byRestaurant.put(restaurantId, current);
            }
            long redemptions = campaign.getRedemptions() != null ? campaign.getRedemptions() : 0L;
            long pointsIssued = campaign.getReach() != null ? campaign.getReach() : 0L;
            current.setRedemptions(current.getRedemptions() + redemptions);
            current.setPointsIssued(current.getPointsIssued() + pointsIssued);
            if ("ACTIVE".equalsIgnoreCase(campaign.getStatus())) {
                current.setActiveOffers(current.getActiveOffers() + 1);
            }
        }

        entries.addAll(byRestaurant.values());
        entries.sort((a, b) -> Long.compare(b.getRedemptions(), a.getRedemptions()));
        List<RewardsOffersPerformanceResponse.LeaderboardEntry> rankedEntries = new ArrayList<>();
        int rank = 1;
        for (RewardsOffersPerformanceResponse.LeaderboardEntry entry : entries.stream().limit(10).toList()) {
            entry.setRank(rank++);
            rankedEntries.add(entry);
        }
        return rankedEntries;
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
