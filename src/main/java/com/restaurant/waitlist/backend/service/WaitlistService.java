package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.JoinWaitlistRequest;
import com.restaurant.waitlist.backend.dto.response.DashboardStatsResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistDashboardStatsResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Table;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
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

    public WaitlistResponse joinWaitlist(JoinWaitlistRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

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

        waitlist = waitlistRepository.save(waitlist);
        return WaitlistResponse.fromWaitlist(waitlist);
    }

    public WaitlistResponse rejoinWaitlist(JoinWaitlistRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        String normalizedPhone = normalizePhone(request.getPhone());
        Waitlist existing = waitlistRepository.findLatestByRestaurantIdAndGuestPhone(request.getRestaurantId(), normalizedPhone)
                .orElse(null);

        if (existing != null && existing.getStatus() != Waitlist.WaitlistStatus.CANCELLED) {
            return WaitlistResponse.fromWaitlist(existing);
        }

        if (existing == null) {
            existing = Waitlist.builder()
                    .restaurant(restaurant)
                    .guestName(request.getName())
                    .guestPhone(normalizedPhone)
                    .partySize(request.getPartySize())
                    .preference(request.getPreference())
                    .notes(request.getNotes())
                    .status(Waitlist.WaitlistStatus.PENDING)
                    .lastActiveStatus(Waitlist.WaitlistStatus.PENDING)
                    .build();
        } else {
            existing.setRestaurant(restaurant);
            existing.setGuestName(request.getName());
            existing.setGuestPhone(normalizedPhone);
            existing.setPartySize(request.getPartySize());
            existing.setPreference(request.getPreference());
            existing.setNotes(request.getNotes());

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
                .map(RestaurantResponse::fromRestaurant)
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

