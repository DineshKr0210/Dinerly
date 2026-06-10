package com.restaurant.waitlist.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.waitlist.backend.dto.response.AnalyticsResponse;
import com.restaurant.waitlist.backend.dto.response.FeedbackInsightsResponse;
import com.restaurant.waitlist.backend.dto.response.GuestHistoryResponse;
import com.restaurant.waitlist.backend.entity.Feedback;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyticsResponse getAnalytics() {
        List<Feedback> allFeedback = feedbackRepository.findAll();
        List<Waitlist> allWaitlists = waitlistRepository.findAll();

        // Total seated
        long totalSeated = allWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.SEATED)
                .count();

        // Average wait time
        double avgWaitTime = allWaitlists.stream()
                .filter(w -> w.getSeatedAt() != null && w.getJoinedAt() != null)
                .mapToLong(w -> ChronoUnit.MINUTES.between(w.getJoinedAt(), w.getSeatedAt()))
                .average()
                .orElse(0.0);

        // No-show rate
        long noShowCount = allWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.NO_SHOW)
                .count();
        double noShowRate = allWaitlists.isEmpty() ? 0 : (double) noShowCount / allWaitlists.size() * 100;

        // Average rating
        double avgRating = allFeedback.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        return AnalyticsResponse.builder()
                .totalSeated(totalSeated)
                .avgWaitTime(Math.round(avgWaitTime * 100.0) / 100.0)
                .noShowRate(Math.round(noShowRate * 100.0) / 100.0)
                .avgRating(Math.round(avgRating * 100.0) / 100.0)
                .build();
    }

    public List<GuestHistoryResponse> getGuestHistory() {
        List<Waitlist> allWaitlists = waitlistRepository.findAll();

        // Filter to only include guests who have been seated
        Map<String, List<Waitlist>> groupedByPhone = allWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.SEATED)
                .collect(Collectors.groupingBy(Waitlist::getGuestPhone));

        return groupedByPhone.entrySet().stream()
                .map(entry -> buildGuestHistoryResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private GuestHistoryResponse buildGuestHistoryResponse(String phone, List<Waitlist> guestWaitlists) {
        long visits = guestWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.SEATED)
                .count();

        double avgWait = guestWaitlists.stream()
                .filter(w -> w.getSeatedAt() != null && w.getJoinedAt() != null)
                .mapToLong(w -> ChronoUnit.MINUTES.between(w.getJoinedAt(), w.getSeatedAt()))
                .average()
                .orElse(0.0);

        LocalDate lastVisit = guestWaitlists.stream()
                .map(w -> w.getSeatedAt() != null ? w.getSeatedAt().toLocalDate() : w.getCreatedAt().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(null);

        double avgRating = guestWaitlists.stream()
                .flatMap(w -> feedbackRepository.findByWaitlistId(w.getId()).stream())
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        return GuestHistoryResponse.builder()
                .name(guestWaitlists.get(0).getGuestName())
                .phone(phone)
                .visits(visits)
                .avgWait(Math.round(avgWait * 100.0) / 100.0)
                .lastVisit(lastVisit)
                .rating(Math.round(avgRating * 100.0) / 100.0)
                .build();
    }

    public FeedbackInsightsResponse getFeedbackInsights() {
        List<Feedback> allFeedback = feedbackRepository.findAll();

        double overallRating = allFeedback.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        Map<String, Integer> tagCounts = new HashMap<>();
        allFeedback.forEach(feedback -> {
            try {
                if (feedback.getTags() != null) {
                    List<String> tags = objectMapper.readValue(feedback.getTags(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                    tags.forEach(tag -> tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1));
                }
            } catch (Exception e) {
                System.err.println("Error parsing tags: " + e.getMessage());
            }
        });

        List<String> topTags = tagCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return FeedbackInsightsResponse.builder()
                .overallRating(Math.round(overallRating * 100.0) / 100.0)
                .totalReviews((long) allFeedback.size())
                .topTags(topTags)
                .build();
    }
}


