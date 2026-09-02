package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.ReviewResponse;
import com.restaurant.waitlist.backend.entity.Feedback;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
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

    @Override
    public Page<ReviewResponse> listReviews(Long locationId, String filter, Pageable pageable) {
        Page<Feedback> page = feedbackRepository.findAll(pageable);

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
        fb.setReply(reply);
        fb.setRepliedAt(LocalDateTime.now());
        feedbackRepository.save(fb);
    }
}
