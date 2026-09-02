package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.WaitlistPerformanceSummary;

public interface AdminPerformanceService {
    WaitlistPerformanceSummary getWaitlistPerformance(Long locationId, String period);
    java.util.Map<String, Object> getReviewsPerformance(String period);
}
