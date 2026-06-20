package com.restaurant.waitlist.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.response.*;
import com.restaurant.waitlist.backend.entity.Feedback;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.repository.spec.WaitlistSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantSettingsRepository restaurantSettingsRepository;

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

    public Page<WaitlistResponse> getGuestHistory(Long restaurantId, Integer page, Integer size,
                                                  String statusStr, String dateStr) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Waitlist.WaitlistStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            status = Waitlist.WaitlistStatus.valueOf(statusStr.trim().toUpperCase());
        }

        java.time.LocalDate date = null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            date = java.time.LocalDate.parse(dateStr.trim());
        }

        Pageable pageable = PageRequest.of(
                page != null && page >= 0 ? page : 0,
                size != null && size > 0 ? size : 10,
                Sort.by(Sort.Direction.DESC, "joinedAt")
        );

        Specification<Waitlist> spec = WaitlistSpecification.filter(restaurantId, status, date, null, null);
        Page<Waitlist> pageResult = waitlistRepository.findAll(spec, pageable);
        return pageResult.map(WaitlistResponse::fromWaitlist);
    }

    public String exportGuestHistoryCsv(Long restaurantId, String statusStr, String dateStr) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Waitlist.WaitlistStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            status = Waitlist.WaitlistStatus.valueOf(statusStr.trim().toUpperCase());
        }

        java.time.LocalDate date = null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            date = java.time.LocalDate.parse(dateStr.trim());
        }

        Specification<Waitlist> spec = WaitlistSpecification.filter(restaurantId, status, date, null, null);
        java.util.List<Waitlist> rows = waitlistRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "joinedAt"));

        StringBuilder sb = new StringBuilder();
        sb.append("id,guestName,phone,partySize,status,joinedAt,approvedAt,notifiedAt,seatedAt,cancelledAt,tableName\n");
        for (Waitlist w : rows) {
            sb.append(w.getId()).append(",");
            sb.append(csvEscape(w.getGuestName())).append(",");
            sb.append(csvEscape(w.getGuestPhone())).append(",");
            sb.append(w.getPartySize() != null ? w.getPartySize() : "").append(",");
            sb.append(w.getStatus() != null ? w.getStatus().name() : "").append(",");
            sb.append(w.getJoinedAt() != null ? w.getJoinedAt().toString() : "").append(",");
            sb.append(w.getApprovedAt() != null ? w.getApprovedAt().toString() : "").append(",");
            sb.append(w.getNotifiedAt() != null ? w.getNotifiedAt().toString() : "").append(",");
            sb.append(w.getSeatedAt() != null ? w.getSeatedAt().toString() : "").append(",");
            sb.append(w.getCancelledAt() != null ? w.getCancelledAt().toString() : "").append(",");
            sb.append(csvEscape(w.getTableName())).append("\n");
        }
        return sb.toString();
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        String escaped = s.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        } else {
            return escaped;
        }
    }

    public ReportsResponse getReports(Long restaurantId, String fromDateStr, String toDateStr) {
        restaurantRepository.findById(restaurantId).orElseThrow(() -> new RuntimeException("Restaurant not found"));

        java.sql.Date fromDate = null;
        java.sql.Date toDate = null;
        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            fromDate = java.sql.Date.valueOf(java.time.LocalDate.parse(fromDateStr.trim()));
        }
        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            toDate = java.sql.Date.valueOf(java.time.LocalDate.parse(toDateStr.trim()));
        }

        long totalGuests = waitlistRepository.countByRestaurantInDateRange(restaurantId, fromDate, toDate);
        long totalWaiting = waitlistRepository.countByRestaurantAndStatusInDateRange(restaurantId, "WAITING", fromDate, toDate);
        long totalNotified = waitlistRepository.countByRestaurantAndStatusInDateRange(restaurantId, "NOTIFIED", fromDate, toDate);
        long totalSeated = waitlistRepository.countByRestaurantAndStatusInDateRange(restaurantId, "SEATED", fromDate, toDate);
        long totalCancelled = waitlistRepository.countByRestaurantAndStatusInDateRange(restaurantId, "CANCELLED", fromDate, toDate);

        Double avgMinutes = waitlistRepository.averageSeatedDurationMinutes(restaurantId, fromDate, toDate);
        int averageWaitTime = avgMinutes != null ? (int) Math.round(avgMinutes) : 0;

        java.time.LocalDate today = java.time.LocalDate.now();
        java.sql.Date todayDate = java.sql.Date.valueOf(today);
        long todayGuestsCount = waitlistRepository.countByRestaurantInDateRange(restaurantId, todayDate, todayDate);
        long todaySeatedCount = waitlistRepository.countByRestaurantAndStatusInDateRange(restaurantId, "SEATED", todayDate, todayDate);

        return ReportsResponse.builder()
                .totalGuests(totalGuests)
                .totalWaiting(totalWaiting)
                .totalNotified(totalNotified)
                .totalSeated(totalSeated)
                .totalCancelled(totalCancelled)
                .averageWaitTime(averageWaitTime)
                .todayGuestsCount(todayGuestsCount)
                .todaySeatedCount(todaySeatedCount)
                .build();
    }

    public RestaurantSettingsResponse getSettings(Long restaurantId) {
        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> {
                    Restaurant restaurant = restaurantRepository.findById(restaurantId)
                            .orElseThrow(() -> new RuntimeException("Restaurant not found"));
                    RestaurantSettings newSettings = RestaurantSettings.builder()
                            .restaurant(restaurant)
                            .build();
                    return restaurantSettingsRepository.save(newSettings);
                });
        return RestaurantSettingsResponse.fromSettings(settings);
    }

    public RestaurantSettingsResponse updateSettings(Long restaurantId, UpdateRestaurantSettingsRequest request) {
        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> {
                    Restaurant restaurant = restaurantRepository.findById(restaurantId)
                            .orElseThrow(() -> new RuntimeException("Restaurant not found"));
                    return RestaurantSettings.builder()
                            .restaurant(restaurant)
                            .build();
                });

        if (request.getSendSmsNotifications() != null) {
            settings.setSendSmsNotifications(request.getSendSmsNotifications());
        }
        if (request.getSendEmailNotifications() != null) {
            settings.setSendEmailNotifications(request.getSendEmailNotifications());
        }
        if (request.getAverageServiceTime() != null) {
            settings.setAverageServiceTime(request.getAverageServiceTime());
        }
        if (request.getBufferTime() != null) {
            settings.setBufferTime(request.getBufferTime());
        }
        if (request.getOperatingHours() != null) {
            settings.setOperatingHours(request.getOperatingHours());
        }
        if (request.getMaxWaitlistSize() != null) {
            settings.setMaxWaitlistSize(request.getMaxWaitlistSize());
        }

        settings = restaurantSettingsRepository.save(settings);
        return RestaurantSettingsResponse.fromSettings(settings);
    }
}


