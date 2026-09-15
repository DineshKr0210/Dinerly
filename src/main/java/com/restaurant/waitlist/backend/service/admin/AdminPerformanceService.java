package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.ReviewsPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RewardsOffersPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.WaitlistPerformanceResponse;

public interface AdminPerformanceService {
    WaitlistPerformanceResponse getWaitlistPerformance(Long locationId, String period);
    ReviewsPerformanceResponse getReviewsPerformance(Long locationId, String period);
    RewardsOffersPerformanceResponse getRewardsOffersPerformance(Long locationId, String period, int page, int size);
}
