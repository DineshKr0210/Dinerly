package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.WaitlistPerformanceSummary;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.admin.AdminPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.sql.Date;

@Service
public class AdminPerformanceServiceImpl implements AdminPerformanceService {

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    public WaitlistPerformanceSummary getWaitlistPerformance(Long locationId, String period) {
        LocalDate to = LocalDate.now();
        LocalDate from = fromPeriod(period, to);

        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        long joins = (locationId == null) ? waitlistRepository.countAllInDateRange(fromDate, toDate) : waitlistRepository.countByRestaurantInDateRange(locationId, fromDate, toDate);
        long seated = (locationId == null) ? waitlistRepository.countByRestaurantAndStatusInDateRange(0L, "SEATED", fromDate, toDate) : waitlistRepository.countByRestaurantAndStatusInDateRange(locationId, "SEATED", fromDate, toDate);

        Double avgWait = (locationId == null) ? waitlistRepository.averageSeatedDurationMinutes(null, fromDate, toDate) : waitlistRepository.averageSeatedDurationMinutes(locationId, fromDate, toDate);

        WaitlistPerformanceSummary s = WaitlistPerformanceSummary.builder()
                .waitlistJoins(joins)
                .waitlistJoinsComparison(null)
                .guestsSeated(seated)
                .guestsSeatedComparison(null)
                .averageWaitTimeMinutes(avgWait)
                .averageWaitTimeComparison(null)
                .build();

        return s;
    }

    @Override
    public java.util.Map<String, Object> getReviewsPerformance(String period) {
        LocalDate to = LocalDate.now();
        LocalDate from = fromPeriod(period, to);
        Date fromDate = Date.valueOf(from);
        Date toDate = Date.valueOf(to);

        Double avg = feedbackRepository.averageRating();
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("averageRating", avg != null ? avg : 0.0);
        map.put("reviewsReceived", feedbackRepository.count());
        return map;
    }

    private LocalDate fromPeriod(String period, LocalDate to) {
        if (period == null) return to.minusDays(7);
        switch (period) {
            case "last7days": return to.minusDays(7);
            case "pastMonth": return to.minusMonths(1);
            case "last3months": return to.minusMonths(3);
            default: return to.minusDays(7);
        }
    }
}
