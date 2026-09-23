package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.ReviewResponse;
import com.restaurant.waitlist.backend.entity.Feedback;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.admin.AdminReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminReviewServiceImpl implements AdminReviewService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private AdminLocationAccessService adminLocationAccessService;

    @Override
    public Page<ReviewResponse> listReviews(Long locationId, String filter, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        Page<Feedback> page = feedbackRepository.findByWaitlistRestaurantIdIn(restaurantIds, pageable);

        List<ReviewResponse> reviews = page.getContent().stream().map(f ->
                ReviewResponse.builder()
                        .id(f.getId())
                        .guest(f.getWaitlist() != null ? f.getWaitlist().getGuestName() : null)
                        .locationId(f.getWaitlist() != null && f.getWaitlist().getRestaurant() != null ? f.getWaitlist().getRestaurant().getId() : null)
                        .location(f.getWaitlist() != null && f.getWaitlist().getRestaurant() != null ? f.getWaitlist().getRestaurant().getName() : null)
                        .rating(f.getRating())
                        .review(f.getComments())
                        .reply(f.getReply())
                        .createdAt(f.getCreatedAt())
                        .repliedAt(f.getRepliedAt())
                        .build()
        ).collect(Collectors.toList());

        return new PageImpl<>(reviews, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public void replyToReview(Long reviewId, String reply) {
        Feedback fb = feedbackRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        Long restaurantId = fb.getWaitlist() != null && fb.getWaitlist().getRestaurant() != null ? fb.getWaitlist().getRestaurant().getId() : null;
        adminLocationAccessService.assertAccess(restaurantId);
        fb.setReply(reply);
        fb.setRepliedAt(LocalDateTime.now());
        feedbackRepository.save(fb);
    }

    @Override
    public com.restaurant.waitlist.backend.dto.response.admin.ReviewAnalyticsResponse getReviewAnalytics(Long locationId, int days) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        List<Feedback> reviews = feedbackRepository.findByWaitlistRestaurantIdIn(restaurantIds);

        if (reviews.isEmpty()) {
            return com.restaurant.waitlist.backend.dto.response.admin.ReviewAnalyticsResponse.builder()
                    .averageRating(0.0)
                    .totalReviews(0L)
                    .needsReplyCount(0L)
                    .replyRate(0.0)
                    .trend("STABLE")
                    .daysAnalyzed(days)
                    .build();
        }

        double averageRating = reviews.stream()
                .mapToInt(f -> f.getRating() != null ? f.getRating() : 0)
                .average()
                .orElse(0.0);

        long needsReply = reviews.stream()
                .filter(f -> f.getReply() == null || f.getReply().isEmpty())
                .count();

        double replyRate = reviews.size() > 0 ? ((reviews.size() - needsReply) * 100.0) / reviews.size() : 0.0;

        String trend = "STABLE";
        if (averageRating > 4.0) trend = "UP";
        else if (averageRating < 3.0) trend = "DOWN";

        long count5 = reviews.stream().filter(f -> f.getRating() != null && f.getRating() == 5).count();
        long count4 = reviews.stream().filter(f -> f.getRating() != null && f.getRating() == 4).count();
        long count3 = reviews.stream().filter(f -> f.getRating() != null && f.getRating() == 3).count();
        long count2 = reviews.stream().filter(f -> f.getRating() != null && f.getRating() == 2).count();
        long count1 = reviews.stream().filter(f -> f.getRating() != null && f.getRating() == 1).count();

        return com.restaurant.waitlist.backend.dto.response.admin.ReviewAnalyticsResponse.builder()
                .averageRating(averageRating)
                .totalReviews((long) reviews.size())
                .needsReplyCount(needsReply)
                .replyRate(replyRate)
                .ratingCount5Star(count5)
                .ratingCount4Star(count4)
                .ratingCount3Star(count3)
                .ratingCount2Star(count2)
                .ratingCount1Star(count1)
                .trend(trend)
                .daysAnalyzed(days)
                .build();
    }

    @Override
    public java.util.Map<String, Long> getRatingDistribution(Long locationId, int days) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        List<Feedback> reviews = feedbackRepository.findByWaitlistRestaurantIdIn(restaurantIds);

        java.util.Map<String, Long> distribution = new java.util.HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            long count = reviews.stream()
                    .filter(f -> f.getRating() != null && f.getRating() == rating)
                    .count();
            distribution.put(rating + " star" + (count != 1 ? "s" : ""), count);
        }

        return distribution;
    }

}
