package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.JoinWaitlistRequest;
import com.restaurant.waitlist.backend.dto.response.DashboardStatsResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistDashboardStatsResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.entity.NotificationSettingsPayload;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.Table;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.entity.WaitlistSettingsPayload;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WaitlistService {

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private SmsService smsService;

    @Autowired
    private RestaurantSettingsRepository restaurantSettingsRepository;

    public WaitlistResponse joinWaitlist(JoinWaitlistRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(request.getRestaurantId()).orElse(null);
        WaitlistSettingsPayload waitlistSettings = settings != null ? settings.getWaitlistSettings() : WaitlistSettingsPayload.defaults();

        if (request.getPartySize() != null && waitlistSettings.getMaxPartySize() != null && request.getPartySize() > waitlistSettings.getMaxPartySize()) {
            throw new RuntimeException("Party size exceeds the configured maximum of " + waitlistSettings.getMaxPartySize());
        }

        if (Boolean.TRUE.equals(waitlistSettings.getWalkInsOnly())) {
            throw new RuntimeException("Online requests are not accepted when walk-ins only is enabled");
        }

        if (Boolean.TRUE.equals(waitlistSettings.getPauseNewJoinsAfterClosing())) {
            String closeTime = restaurant.getCloseTime();
            if (closeTime != null && !closeTime.isBlank()) {
                LocalTime closingTime = LocalTime.parse(closeTime);
                LocalTime currentTime = LocalTime.now();
                if (!currentTime.isBefore(closingTime)) {
                    throw new RuntimeException("New joins are paused after the restaurant closing time");
                }
            }
        }

        if (Boolean.FALSE.equals(waitlistSettings.getAcceptOnlineJoin())) {
            throw new RuntimeException("Online join requests are currently disabled");
        }

        Waitlist waitlist = Waitlist.builder()
                .restaurant(restaurant)
                .guestName(request.getName())
                .guestPhone(normalizePhone(request.getPhone()))
                .partySize(request.getPartySize())
                .preference(request.getPreference())
                .notes(request.getNotes())
                .status(Waitlist.WaitlistStatus.PENDING)
                .lastActiveStatus(Waitlist.WaitlistStatus.PENDING)
                .build();


        NotificationSettingsPayload payload = settings != null ? settings.getNotificationSettings() : NotificationSettingsPayload.defaults();
        boolean shouldSendJoinSms = payload.getGuestNotifications() != null
                && Boolean.TRUE.equals(payload.getGuestNotifications().getJoinedwaitlistsmsenabled());

        if (shouldSendJoinSms) {
            try {
                String message = smsService.sendJoinConfirmationSms(request.getRestaurantId(), waitlist.getGuestPhone(), waitlist.getGuestName());
                waitlist.setSmsMessage(message);
                waitlist.setSmsStatus("SENT");
                waitlist.setSmsSentAt(LocalDateTime.now());
                waitlist.setSmsError(null);
            } catch (Exception e) {
                waitlist.setSmsStatus("FAILED");
                waitlist.setSmsError(e.getMessage());
            }
        } else {
            waitlist.setSmsStatus("DISABLED");
            waitlist.setSmsMessage("Join confirmation SMS disabled in settings");
            waitlist.setSmsError(null);
        }

        waitlist = waitlistRepository.save(waitlist);
        return WaitlistResponse.fromWaitlist(waitlist);
    }

    public WaitlistResponse rejoinWaitlist(Long restaurantId, Long waitlistId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Waitlist existing = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));

        if (existing.getRestaurant() == null || !existing.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Waitlist entry does not belong to the specified restaurant");
        }

        if (existing.getStatus() != Waitlist.WaitlistStatus.CANCELLED) {
            return WaitlistResponse.fromWaitlist(existing);
        }

        existing.setRestaurant(restaurant);

        Waitlist.WaitlistStatus restoredStatus = existing.getLastActiveStatus() != null
                ? existing.getLastActiveStatus()
                : resolveRejoinStatus(existing);

        existing.setStatus(restoredStatus);
        existing.setLastActiveStatus(restoredStatus);
        existing.setCancelledAt(null);

        if (existing.getStatus() == Waitlist.WaitlistStatus.WAITING || existing.getStatus() == Waitlist.WaitlistStatus.NOTIFIED) {
            long activeCount = waitlistRepository.findByRestaurantIdAndJoinedDate(restaurant.getId(), java.sql.Date.valueOf(java.time.LocalDate.now())).stream()
                    .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.WAITING || w.getStatus() == Waitlist.WaitlistStatus.NOTIFIED)
                    .count();
            existing.setPosition((int) activeCount + 1);
        } else {
            existing.setPosition(null);
        }

        existing = waitlistRepository.save(existing);
        return WaitlistResponse.fromWaitlist(existing);
    }

    public WaitlistResponse getWaitlistStatus(Long restaurantId, String phone) {
        // fetch the latest waitlist entry for this phone at the restaurant (most recent joinedAt, then by id)
        Waitlist waitlist = waitlistRepository
                .findLatestByRestaurantIdAndGuestPhone(restaurantId, phone)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found for this restaurant"));

        return WaitlistResponse.fromWaitlist(waitlist);
    }

    public List<WaitlistResponse> getWaitlistStatusList(Long restaurantId, String phone) {
        List<Waitlist> waitlists = waitlistRepository.findAllByRestaurantIdAndGuestPhone(restaurantId, phone);
        
        if (waitlists.isEmpty()) {
            throw new RuntimeException("No waitlist entries found for this phone number");
        }
        
        return waitlists.stream()
                .map(WaitlistResponse::fromWaitlist)
                .collect(Collectors.toList());
    }

    public WaitlistResponse removeFromWaitlist(Long restaurantId, Long waitlistId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));

        if (waitlist.getRestaurant() == null || !waitlist.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Waitlist entry does not belong to the specified restaurant");
        }

        waitlist.setLastActiveStatus(waitlist.getStatus() == null || waitlist.getStatus() == Waitlist.WaitlistStatus.CANCELLED
                ? waitlist.getLastActiveStatus()
                : waitlist.getStatus());
        waitlist.setStatus(Waitlist.WaitlistStatus.CANCELLED);
        waitlist.setCancelledAt(java.time.LocalDateTime.now());
        waitlistRepository.save(waitlist);

        return WaitlistResponse.fromWaitlist(waitlist);
    }
    public WaitlistDashboardStatsResponse getDashboardStats(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        List<Waitlist> allWaitlists = waitlistRepository.findByRestaurantId(restaurantId);

        List<Waitlist> todayWaitlists = allWaitlists.stream()
                .filter(w -> w.getJoinedAt().isAfter(startOfDay) && w.getJoinedAt().isBefore(endOfDay))
                .toList();

        long totalWaiting = todayWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.WAITING || w.getStatus() == Waitlist.WaitlistStatus.NOTIFIED)
                .count();

        Integer avgWaitTime = (int) todayWaitlists.stream()
                .filter(w -> (w.getStatus() == Waitlist.WaitlistStatus.WAITING || w.getStatus() == Waitlist.WaitlistStatus.NOTIFIED) && w.getEstimatedWaitTime() != null)
                .mapToInt(Waitlist::getEstimatedWaitTime)
                .average()
                .orElse(0);


        return WaitlistDashboardStatsResponse.builder()
                .totalWaiting(totalWaiting)
                .averageWaitTime(avgWaitTime)
                .build();
    }

    public List<WaitlistResponse> getRestaurantWaitlist(Long restaurantId) {
        List<Waitlist> waitlist = waitlistRepository.findByRestaurantId(restaurantId);
        return waitlist.stream()
                .map(WaitlistResponse::fromWaitlist)
                .collect(Collectors.toList());
    }

    public List<WaitlistResponse> getAllWaitlist() {
        List<Waitlist> waitlist = waitlistRepository.findAll();
        return waitlist.stream()
                .map(WaitlistResponse::fromWaitlist)
                .collect(Collectors.toList());
    }

    public Waitlist getWaitlistById(Long id) {
        return waitlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));
    }

    public Waitlist getWaitlistById(Long restaurantId, Long id) {
        Waitlist w = getWaitlistById(id);
        if (w.getRestaurant() == null || !w.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Waitlist entry does not belong to the specified restaurant");
        }
        return w;
    }

    public void markAsSeated(Long restaurantId, Long waitlistId) {
        Waitlist waitlist = getWaitlistById(restaurantId, waitlistId);

        if (waitlist.getStatus() == Waitlist.WaitlistStatus.CANCELLED) {
            throw new RuntimeException("Waitlist entry was already cancelled");
        }

        // If already seated, no-op or inform
        if (waitlist.getStatus() == Waitlist.WaitlistStatus.SEATED) {
            throw new RuntimeException("Waitlist entry is already seated");
        }

        waitlist.setStatus(Waitlist.WaitlistStatus.SEATED);
        waitlist.setSeatedAt(java.time.LocalDateTime.now());
        waitlistRepository.save(waitlist);
    }

    public void markAsNotified(Long restaurantId, Long waitlistId) {
        Waitlist waitlist = getWaitlistById(restaurantId, waitlistId);
        waitlist.setStatus(Waitlist.WaitlistStatus.NOTIFIED);
        waitlistRepository.save(waitlist);
    }

    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(restaurant -> {
                    RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurant.getId())
                            .orElse(null);
                    return RestaurantResponse.fromRestaurant(restaurant, settings);
                })
                .collect(Collectors.toList());
    }

    private Waitlist.WaitlistStatus resolveRejoinStatus(Waitlist waitlist) {
        if (waitlist.getNotifiedAt() != null) {
            return Waitlist.WaitlistStatus.NOTIFIED;
        }
        if (waitlist.getApprovedAt() != null) {
            return Waitlist.WaitlistStatus.WAITING;
        }
        return Waitlist.WaitlistStatus.PENDING;
    }

    private String normalizePhone(String value) {
        if (value == null) {
            return null;
        }
        String s = value.trim();
        if (s.isBlank()) {
            return s;
        }
        String digits = s.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return s;
        }
        if (s.startsWith("+")) {
            return "+" + digits;
        }
        if (digits.length() == 10) {
            return "+91" + digits;
        }
        if (digits.length() == 11 && digits.startsWith("0")) {
            return "+91" + digits.substring(1);
        }
        if (digits.length() >= 11 && (digits.startsWith("91") || digits.length() > 10)) {
            return "+" + digits;
        }
        return digits;
    }
}

