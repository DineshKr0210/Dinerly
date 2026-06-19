package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.NotificationSummaryResponse;
import com.restaurant.waitlist.backend.dto.response.SendSmsResponse;
import com.restaurant.waitlist.backend.dto.response.SmsHistoryResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private SmsService smsService;

    public NotificationSummaryResponse getSummary(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        List<Waitlist> todayWaitlists = waitlistRepository.findByRestaurantId(restaurantId).stream()
                .filter(w -> w.getJoinedAt().isAfter(startOfDay) && w.getJoinedAt().isBefore(endOfDay))
                .toList();

        long totalGuests = todayWaitlists.size();
        long waiting = todayWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.WAITING)
                .count();
        long notified = todayWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.NOTIFIED)
                .count();
        long seated = todayWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.SEATED)
                .count();
        long cancelled = todayWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.CANCELLED)
                .count();

        return NotificationSummaryResponse.builder()
                .totalGuests(totalGuests)
                .waiting(waiting)
                .notified(notified)
                .seated(seated)
                .cancelled(cancelled)
                .build();
    }

    public Page<WaitlistResponse> getNotifications(Long restaurantId, Pageable pageable, String search, String status) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Waitlist.WaitlistStatus statusFilter = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusFilter = Waitlist.WaitlistStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
        }

        Page<Waitlist> page = waitlistRepository.findByRestaurantIdWithSearch(restaurantId, statusFilter, search, pageable);
        return page.map(WaitlistResponse::fromWaitlist);
    }

    public WaitlistResponse getNotificationDetail(Long restaurantId, Long waitlistId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));

        if (waitlist.getRestaurant() == null || !waitlist.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Waitlist entry does not belong to the specified restaurant");
        }

        return WaitlistResponse.fromWaitlist(waitlist);
    }

    public SendSmsResponse sendSms(Long restaurantId, Long waitlistId, String message) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));

        if (waitlist.getRestaurant() == null || !waitlist.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Waitlist entry does not belong to the specified restaurant");
        }

        try {
            smsService.sendSms(waitlist.getGuestPhone(), message);
            waitlist.setSmsMessage(message);
            waitlist.setSmsStatus("SENT");
            waitlist.setSmsSentAt(LocalDateTime.now());
            waitlist.setSmsError(null);
            waitlistRepository.save(waitlist);
            return SendSmsResponse.builder().smsSent(true).build();
        } catch (Exception e) {
            waitlist.setSmsMessage(message);
            waitlist.setSmsStatus("FAILED");
            waitlist.setSmsError(e.getMessage());
            waitlistRepository.save(waitlist);
            return SendSmsResponse.builder()
                    .smsSent(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    public SmsHistoryResponse getSmsHistory(Long restaurantId, Long waitlistId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));

        if (waitlist.getRestaurant() == null || !waitlist.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Waitlist entry does not belong to the specified restaurant");
        }

        return SmsHistoryResponse.builder()
                .smsMessage(waitlist.getSmsMessage())
                .smsStatus(waitlist.getSmsStatus())
                .smsError(waitlist.getSmsError())
                .smsSentAt(waitlist.getSmsSentAt())
                .build();
    }
}


