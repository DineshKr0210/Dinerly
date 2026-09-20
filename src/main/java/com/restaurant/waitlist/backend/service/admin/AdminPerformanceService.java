package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.ReviewsPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RewardsOffersPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.WaitlistPerformanceResponse;
import java.time.LocalDate;

public interface AdminPerformanceService {
    /**
     * Get waitlist performance metrics.
     * Either use period (pastMonth, pastWeek, etc.) OR provide custom startDate/endDate.
     * Custom dates take precedence over period if both are provided.
     */
    WaitlistPerformanceResponse getWaitlistPerformance(Long locationId, String period, LocalDate startDate, LocalDate endDate);

    /**
     * Get reviews performance metrics.
     * Either use period (pastMonth, pastWeek, etc.) OR provide custom startDate/endDate.
     * Custom dates take precedence over period if both are provided.
     */
    ReviewsPerformanceResponse getReviewsPerformance(Long locationId, String period, LocalDate startDate, LocalDate endDate);

    /**
     * Get rewards & offers performance metrics.
     * Either use period (pastMonth, pastWeek, etc.) OR provide custom startDate/endDate.
     * Custom dates take precedence over period if both are provided.
     */
    RewardsOffersPerformanceResponse getRewardsOffersPerformance(Long locationId, String period, LocalDate startDate, LocalDate endDate, int page, int size);
}
