package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.AddGuestRequest;
import com.restaurant.waitlist.backend.dto.request.CreateRestaurantRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.response.DashboardStatsResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantSettingsResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.Table;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.TableRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private RestaurantSettingsRepository restaurantSettingsRepository;

    @Autowired
    private WaitlistService waitlistService;

    @Autowired
    private SmsService smsService;

    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .totalTables(request.getTotalTables() != null ? request.getTotalTables() : 0)
                .build();

        restaurant = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromRestaurant(restaurant);
    }

    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(RestaurantResponse::fromRestaurant)
                .collect(Collectors.toList());
    }

    public WaitlistResponse addGuestToWaitlist(Long restaurantId, AddGuestRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Waitlist waitlist = Waitlist.builder()
                .restaurant(restaurant)
                .guestName(request.getName())
                .guestPhone(request.getPhone())
                .partySize(request.getPartySize())
                .preference(request.getPreference())
                .notes(request.getNotes())
                .position(request.getPosition())
                .estimatedWaitTime(request.getEstimatedWaitTime())
                .status(Waitlist.WaitlistStatus.WAITING)
                .build();

        waitlist = waitlistRepository.save(waitlist);
        return WaitlistResponse.fromWaitlist(waitlist);
    }

    public List<WaitlistResponse> getWaitlist(Long restaurantId) {
        List<Waitlist> waitlist = waitlistRepository.findByRestaurantId(restaurantId);
        return waitlist.stream()
                .map(WaitlistResponse::fromWaitlist)
                .collect(Collectors.toList());
    }

    public void notifyGuest(Long restaurantId, Long waitlistId, com.restaurant.waitlist.backend.dto.request.NotifyGuestRequest request) {
        // fetch waitlist entry and possibly update estimated time/position
        Waitlist waitlist = waitlistService.getWaitlistById(restaurantId, waitlistId);

        boolean changed = false;
        if (waitlist.getEstimatedWaitTime() == null && request.getEstimatedWaitTime() != null) {
            waitlist.setEstimatedWaitTime(request.getEstimatedWaitTime());
            changed = true;
        }
        if (waitlist.getPosition() == null && request.getPosition() != null) {
            waitlist.setPosition(request.getPosition());
            changed = true;
        }

        if (changed) {
            waitlistRepository.save(waitlist);
        }

        // mark as notified (validates restaurant ownership)
        waitlistService.markAsNotified(restaurantId, waitlistId);

        // send sms using the final values (existing in DB or provided)
        String estimatedTime = waitlist.getEstimatedWaitTime() != null ?
                waitlist.getEstimatedWaitTime().toString() : "Soon";
        Integer position = waitlist.getPosition();

        smsService.sendWaitlistNotificationSms(
                waitlist.getGuestPhone(),
                waitlist.getGuestName(),
                estimatedTime,
                position
        );
    }

    public void seatGuest(Long restaurantId, Long waitlistId) {
        // mark as seated (validates restaurant ownership)
        waitlistService.markAsSeated(restaurantId, waitlistId);

        // send sms
        Waitlist waitlist = waitlistService.getWaitlistById(restaurantId, waitlistId);
        smsService.sendSeatedNotificationSms(
                waitlist.getGuestPhone(),
                waitlist.getGuestName()
        );
    }

    public void removeGuest(Long restaurantId, Long waitlistId) {
        waitlistService.removeFromWaitlist(restaurantId, waitlistId);
    }

    public DashboardStatsResponse getDashboardStats(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        List<Waitlist> allWaitlists = waitlistRepository.findByRestaurantId(restaurantId);

        long totalWaiting = allWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.WAITING)
                .count();

        long totalNotified = allWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.NOTIFIED)
                .count();

        long seatedToday = allWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.SEATED &&
                        w.getSeatedAt() != null &&
                        w.getSeatedAt().isAfter(startOfDay) &&
                        w.getSeatedAt().isBefore(endOfDay))
                .count();

        long noShowsToday = allWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.NO_SHOW &&
                        w.getJoinedAt().isAfter(startOfDay) &&
                        w.getJoinedAt().isBefore(endOfDay))
                .count();

        Integer avgWaitTime = (int) allWaitlists.stream()
                .filter(w -> w.getEstimatedWaitTime() != null)
                .mapToInt(Waitlist::getEstimatedWaitTime)
                .average()
                .orElse(0);

        List<Table> allTables = tableRepository.findByRestaurantId(restaurantId);

        long openTables = allTables.stream()
                .filter(t -> t.getStatus() == Table.TableStatus.OPEN)
                .count();

        long occupiedTables = allTables.stream()
                .filter(t -> t.getStatus() == Table.TableStatus.OCCUPIED)
                .count();

        long reservedTables = allTables.stream()
                .filter(t -> t.getStatus() == Table.TableStatus.RESERVED)
                .count();

        long needsCleaningTables = allTables.stream()
                .filter(t -> t.getStatus() == Table.TableStatus.NEEDS_CLEANING)
                .count();

        return DashboardStatsResponse.builder()
                .totalWaiting(totalWaiting)
                .totalNotified(totalNotified)
                .averageWaitTime(avgWaitTime)
                .seatedToday(seatedToday)
                .noShowsToday(noShowsToday)
                .openTables(openTables)
                .occupiedTables(occupiedTables)
                .reservedTables(reservedTables)
                .tablesNeedingCleaning(needsCleaningTables)
                .build();
    }

    public List<WaitlistResponse> getGuestHistory(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        List<Waitlist> history = waitlistRepository.findByRestaurantId(restaurantId);
        return history.stream()
                .map(WaitlistResponse::fromWaitlist)
                .collect(Collectors.toList());
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

