package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantSummaryService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantSettingsRepository restaurantSettingsRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 23 * * ?")
    public void sendNightlySummaries() {
        List<Long> restaurantIds = restaurantRepository.findAll().stream()
                .map(r -> r.getId())
                .collect(Collectors.toList());
        for (Long restaurantId : restaurantIds) {
            RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                    .orElse(null);
            if (settings == null || !Boolean.TRUE.equals(settings.getSendEmailNotifications()) || settings.getNightlySummaryEmail() == null || settings.getNightlySummaryEmail().isBlank()) {
                continue;
            }
            sendSummaryForRestaurant(restaurantId, settings);
        }
    }

    private void sendSummaryForRestaurant(Long restaurantId, RestaurantSettings settings) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        List<Waitlist> todaysWaitlists = waitlistRepository.findByRestaurantId(restaurantId).stream()
                .filter(w -> w.getJoinedAt() != null && w.getJoinedAt().isAfter(startOfDay) && w.getJoinedAt().isBefore(endOfDay))
                .collect(Collectors.toList());

        long totalGuests = todaysWaitlists.size();
        long waiting = todaysWaitlists.stream().filter(w -> w.getStatus() == Waitlist.WaitlistStatus.WAITING).count();
        long notified = todaysWaitlists.stream().filter(w -> w.getStatus() == Waitlist.WaitlistStatus.NOTIFIED).count();
        long seated = todaysWaitlists.stream().filter(w -> w.getStatus() == Waitlist.WaitlistStatus.SEATED).count();
        long cancelled = todaysWaitlists.stream().filter(w -> w.getStatus() == Waitlist.WaitlistStatus.CANCELLED).count();

        String restaurantName = settings.getRestaurant() != null ? settings.getRestaurant().getName() : "Restaurant";
        String summary = "Nightly Summary for " + restaurantName + "\n\n"
                + "Date: " + today + "\n"
                + "Total guests today: " + totalGuests + "\n"
                + "Waiting: " + waiting + "\n"
                + "Notified: " + notified + "\n"
                + "Seated: " + seated + "\n"
                + "Cancelled: " + cancelled + "\n";

        emailService.sendNightlySummary(settings.getNightlySummaryEmail(), restaurantName, summary);
    }
}
