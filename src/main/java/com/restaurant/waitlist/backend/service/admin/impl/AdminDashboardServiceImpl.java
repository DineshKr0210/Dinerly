package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.AdminDashboardResponse;
import com.restaurant.waitlist.backend.dto.response.admin.LocationLeaderboardItem;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

        // active waitlists are those in WAITING or NOTIFIED
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
}
