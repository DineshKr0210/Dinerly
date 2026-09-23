package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.ReviewsPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RewardsOffersPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.WaitlistPerformanceResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
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

    @Autowired
    private AdminLocationAccessService adminLocationAccessService;

    @Override
    public WaitlistPerformanceResponse getWaitlistPerformance(Long locationId, String period, LocalDate startDate, LocalDate endDate) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        // Use custom dates if provided, otherwise use period-based dates
        LocalDate to = endDate != null ? endDate : LocalDate.now();
        LocalDate from = startDate != null ? startDate : fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        // Current period metrics
        long joins = sumJoins(restaurantIds, fromDate, toDate);
        long seated = sumSeated(restaurantIds, fromDate, toDate);
        Double avgWait = weightedAvgWait(restaurantIds, fromDate, toDate);

        // Previous period metrics for comparison
        LocalDate prevFrom = getPreviousPeriodStart(period, from, startDate);
        LocalDate prevTo = from.minusDays(1);
        Date prevFromDate = Date.valueOf(prevFrom);
        Date prevToDate = Date.valueOf(prevTo);

        long prevJoins = sumJoins(restaurantIds, prevFromDate, prevToDate);
        long prevSeated = sumSeated(restaurantIds, prevFromDate, prevToDate);
        Double prevAvgWait = weightedAvgWait(restaurantIds, prevFromDate, prevToDate);

        List<WaitlistPerformanceResponse.TrendPoint> trend = buildWaitlistTrend(restaurantIds, from, to);
        List<WaitlistPerformanceResponse.LeaderboardEntry> leaderboard = buildWaitlistLeaderboard(restaurantIds, fromDate, toDate);

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
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        // Use custom dates if provided, otherwise use period-based dates
        LocalDate to = endDate != null ? endDate : LocalDate.now();
        LocalDate from = startDate != null ? startDate : fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        // Current period reviews
        long reviewsReceived = sumReviews(restaurantIds, fromDate, toDate);
        Double avgRating = weightedAvgRating(restaurantIds, fromDate, toDate);
        long replied = sumReplied(restaurantIds, fromDate, toDate);
        long replyRate = reviewsReceived > 0 ? Math.round((replied * 100.0) / reviewsReceived) : 0L;

        // Previous period reviews
        LocalDate prevFrom = getPreviousPeriodStart(period, from, startDate);
        LocalDate prevTo = from.minusDays(1);
        Date prevFromDate = Date.valueOf(prevFrom);
        Date prevToDate = Date.valueOf(prevTo);

        long prevReviewsReceived = sumReviews(restaurantIds, prevFromDate, prevToDate);
        Double prevAvgRating = weightedAvgRating(restaurantIds, prevFromDate, prevToDate);
        long prevReplied = sumReplied(restaurantIds, prevFromDate, prevToDate);
        long prevReplyRate = prevReviewsReceived > 0 ? Math.round((prevReplied * 100.0) / prevReviewsReceived) : 0L;

        List<ReviewsPerformanceResponse.TrendPoint> trend = buildReviewsTrend(restaurantIds, from, to);
        List<ReviewsPerformanceResponse.LeaderboardEntry> leaderboard = buildReviewsLeaderboard(restaurantIds, fromDate, toDate);

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
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        // Use custom dates if provided, otherwise use period-based dates
        LocalDate to = endDate != null ? endDate : LocalDate.now();
        LocalDate from = startDate != null ? startDate : fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        // Current period campaigns/redemptions
        List<com.restaurant.waitlist.backend.entity.Campaign> campaigns = campaignRepository.findByRestaurantIdIn(restaurantIds);

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

        List<com.restaurant.waitlist.backend.entity.Campaign> prevCampaigns = campaigns;

        long prevActiveOffers = prevCampaigns.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();
        long prevRedemptions = prevCampaigns.stream()
                .filter(c -> c.getRedemptions() != null)
                .mapToLong(com.restaurant.waitlist.backend.entity.Campaign::getRedemptions)
                .sum();
        long prevPointsIssued = prevCampaigns.stream()
                .filter(c -> c.getReach() != null)
                .mapToLong(com.restaurant.waitlist.backend.entity.Campaign::getReach)
                .sum();

        List<RewardsOffersPerformanceResponse.TrendPoint> trend = buildRewardsTrend(restaurantIds, from, to);
        List<RewardsOffersPerformanceResponse.LeaderboardEntry> leaderboard = buildRewardsLeaderboard(campaigns);

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

    private long sumJoins(List<Long> restaurantIds, Date fromDate, Date toDate) {
        return waitlistRepository.countByRestaurantIdsInDateRange(restaurantIds, fromDate, toDate);
    }

    private long sumSeated(List<Long> restaurantIds, Date fromDate, Date toDate) {
        return waitlistRepository.countByRestaurantIdsAndStatusInDateRangeGrouped(restaurantIds, "SEATED", fromDate, toDate).stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();
    }

    private Double weightedAvgWait(List<Long> restaurantIds, Date fromDate, Date toDate) {
        Map<Long, Long> weights = groupedLongsByRestaurant(
                waitlistRepository.countByRestaurantIdsAndStatusInDateRangeGrouped(restaurantIds, "SEATED", fromDate, toDate));
        Map<Long, Double> avgs = groupedDoublesByRestaurant(
                waitlistRepository.averageSeatedDurationMinutesGrouped(restaurantIds, fromDate, toDate));

        long totalWeight = 0;
        double totalSum = 0;
        for (Long id : restaurantIds) {
            long weight = weights.getOrDefault(id, 0L);
            Double avg = avgs.get(id);
            if (weight > 0 && avg != null) {
                totalSum += avg * weight;
                totalWeight += weight;
            }
        }
        return totalWeight > 0 ? totalSum / totalWeight : 0.0;
    }

    private long sumReviews(List<Long> restaurantIds, Date fromDate, Date toDate) {
        return feedbackRepository.countByWaitlistRestaurantIdInAndDateRange(restaurantIds, fromDate, toDate);
    }

    private long sumReplied(List<Long> restaurantIds, Date fromDate, Date toDate) {
        return feedbackRepository.countRepliedByRestaurantIdInAndDateRange(restaurantIds, fromDate, toDate);
    }

    private Double weightedAvgRating(List<Long> restaurantIds, Date fromDate, Date toDate) {
        Map<Long, Long> weights = groupedLongsByRestaurant(
                feedbackRepository.countByRestaurantIdsAndDateRangeGrouped(restaurantIds, fromDate, toDate));
        Map<Long, Double> avgs = groupedDoublesByRestaurant(
                feedbackRepository.averageRatingByRestaurantIdsAndDateRangeGrouped(restaurantIds, fromDate, toDate));

        long totalWeight = 0;
        double totalSum = 0;
        for (Long id : restaurantIds) {
            long weight = weights.getOrDefault(id, 0L);
            Double avg = avgs.get(id);
            if (weight > 0 && avg != null) {
                totalSum += avg * weight;
                totalWeight += weight;
            }
        }
        return totalWeight > 0 ? totalSum / totalWeight : 0.0;
    }

    private Map<Long, Long> groupedLongsByRestaurant(List<Object[]> rows) {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            Long restaurantId = ((Number) row[0]).longValue();
            long value = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            result.put(restaurantId, value);
        }
        return result;
    }

    private Map<Long, Double> groupedDoublesByRestaurant(List<Object[]> rows) {
        Map<Long, Double> result = new HashMap<>();
        for (Object[] row : rows) {
            Long restaurantId = ((Number) row[0]).longValue();
            Double value = row[1] != null ? ((Number) row[1]).doubleValue() : null;
            result.put(restaurantId, value);
        }
        return result;
    }

    private List<WaitlistPerformanceResponse.TrendPoint> buildWaitlistTrend(List<Long> restaurantIds, LocalDate from, LocalDate to) {
        List<Object[]> rows = waitlistRepository.countJoinsByDayForRestaurantIds(restaurantIds, Date.valueOf(from), Date.valueOf(to));
        Map<LocalDate, Long> values = aggregateCountsByDay(rows);
        return fillDayRange(from, to, values, (date, label, value) -> WaitlistPerformanceResponse.TrendPoint.builder()
                .date(date)
                .label(label)
                .value(value)
                .build());
    }

    private List<ReviewsPerformanceResponse.TrendPoint> buildReviewsTrend(List<Long> restaurantIds, LocalDate from, LocalDate to) {
        List<Object[]> rows = feedbackRepository.countReviewsByDayForRestaurantIds(restaurantIds, Date.valueOf(from), Date.valueOf(to));
        Map<LocalDate, Long> values = aggregateCountsByDay(rows);
        return fillDayRange(from, to, values, (date, label, value) -> ReviewsPerformanceResponse.TrendPoint.builder()
                .date(date)
                .label(label)
                .value(value)
                .build());
    }

    private List<RewardsOffersPerformanceResponse.TrendPoint> buildRewardsTrend(List<Long> restaurantIds, LocalDate from, LocalDate to) {
        List<Object[]> rows = campaignRepository.aggregateRedemptionsByDayForRestaurantIds(restaurantIds, Date.valueOf(from), Date.valueOf(to));
        Map<LocalDate, Long> values = aggregateCountsByDay(rows);
        return fillDayRange(from, to, values, (date, label, value) -> RewardsOffersPerformanceResponse.TrendPoint.builder()
                .date(date)
                .label(label)
                .value(value)
                .build());
    }

    private Map<LocalDate, Long> aggregateCountsByDay(List<Object[]> rows) {
        Map<LocalDate, Long> values = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate date = normalizeDate(row[0]);
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            values.put(date, count);
        }
        return values;
    }

    private <T> List<T> fillDayRange(LocalDate from, LocalDate to, Map<LocalDate, Long> values, TrendPointFactory<T> factory) {
        List<T> trend = new ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            trend.add(factory.create(cursor.toString(), dayLabel(cursor), values.getOrDefault(cursor, 0L)));
            cursor = cursor.plusDays(1);
        }
        return trend;
    }

    @FunctionalInterface
    private interface TrendPointFactory<T> {
        T create(String date, String label, Long value);
    }

    private List<WaitlistPerformanceResponse.LeaderboardEntry> buildWaitlistLeaderboard(List<Long> restaurantIds, Date fromDate, Date toDate) {
        List<WaitlistPerformanceResponse.LeaderboardEntry> entries = new ArrayList<>();
        List<Object[]> rows = waitlistRepository.topRestaurantByJoinsForLocations(restaurantIds, fromDate, toDate);
        rows.sort((a, b) -> Long.compare(
                b[2] != null ? ((Number) b[2]).longValue() : 0L,
                a[2] != null ? ((Number) a[2]).longValue() : 0L));
        if (rows.size() > 10) {
            rows = rows.subList(0, 10);
        }
        if (rows.isEmpty()) {
            if (restaurantIds.size() == 1) {
                Long locationId = restaurantIds.get(0);
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

    private List<ReviewsPerformanceResponse.LeaderboardEntry> buildReviewsLeaderboard(List<Long> restaurantIds, Date fromDate, Date toDate) {
        List<ReviewsPerformanceResponse.LeaderboardEntry> entries = new ArrayList<>();
        List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);

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

    private List<RewardsOffersPerformanceResponse.LeaderboardEntry> buildRewardsLeaderboard(List<com.restaurant.waitlist.backend.entity.Campaign> campaigns) {
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
