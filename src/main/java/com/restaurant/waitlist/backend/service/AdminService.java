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

    @Autowired
    private SmsService smsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyticsResponse getAnalytics() {
        LocalDate today = LocalDate.now();
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.of(today, java.time.LocalTime.MIN);
        java.time.LocalDateTime endOfDay = java.time.LocalDateTime.of(today, java.time.LocalTime.MAX);

        List<Feedback> allFeedback = feedbackRepository.findAll();
        List<Waitlist> allWaitlists = waitlistRepository.findAll();

        List<Waitlist> todayWaitlists = allWaitlists.stream()
                .filter(w -> w.getJoinedAt().isAfter(startOfDay) && w.getJoinedAt().isBefore(endOfDay))
                .toList();

        long totalSeated = todayWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.SEATED)
                .count();

        double avgWaitTime = todayWaitlists.stream()
                .filter(w -> w.getSeatedAt() != null && w.getJoinedAt() != null)
                .mapToLong(w -> ChronoUnit.MINUTES.between(w.getJoinedAt(), w.getSeatedAt()))
                .average()
                .orElse(0.0);

        long noShowCount = todayWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.CANCELLED)
                .count();
        double noShowRate = todayWaitlists.isEmpty() ? 0 : (double) noShowCount / todayWaitlists.size() * 100;

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
        LocalDate today = LocalDate.now();
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.of(today, java.time.LocalTime.MIN);
        java.time.LocalDateTime endOfDay = java.time.LocalDateTime.of(today, java.time.LocalTime.MAX);

        List<Waitlist> allWaitlists = waitlistRepository.findAll();

        Map<String, List<Waitlist>> groupedByPhone = allWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.SEATED &&
                        w.getJoinedAt().isAfter(startOfDay) && w.getJoinedAt().isBefore(endOfDay))
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
        LocalDate today = LocalDate.now();
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.of(today, java.time.LocalTime.MIN);
        java.time.LocalDateTime endOfDay = java.time.LocalDateTime.of(today, java.time.LocalTime.MAX);

        List<Waitlist> allWaitlists = waitlistRepository.findAll();
        Set<Long> todayWaitlistIds = allWaitlists.stream()
                .filter(w -> w.getJoinedAt().isAfter(startOfDay) && w.getJoinedAt().isBefore(endOfDay))
                .map(Waitlist::getId)
                .collect(Collectors.toSet());

        List<Feedback> todayFeedback = feedbackRepository.findAll().stream()
                .filter(f -> f.getWaitlist() != null && todayWaitlistIds.contains(f.getWaitlist().getId()))
                .toList();

        double overallRating = todayFeedback.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        Map<String, Integer> tagCounts = new HashMap<>();
        todayFeedback.forEach(feedback -> {
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
                .totalReviews((long) todayFeedback.size())
                .topTags(topTags)
                .build();
    }

    public java.util.Map<String, String> getTwilioProbe() {
        return smsService.probeAccount();
    }
}


