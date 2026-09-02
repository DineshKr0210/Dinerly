package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminReviewService {
    Page<ReviewResponse> listReviews(Long locationId, String filter, Pageable pageable);
    void replyToReview(Long reviewId, String reply);
}
