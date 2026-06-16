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

        if (waitlist.getPosition() == null) {
            java.sql.Date sqlDate = java.sql.Date.valueOf(java.time.LocalDate.now());
            long activeCount = waitlistRepository.findByRestaurantIdAndJoinedDate(restaurantId, sqlDate).stream()
                    .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.WAITING || w.getStatus() == Waitlist.WaitlistStatus.NOTIFIED)
                    .count();
            waitlist.setPosition((int) activeCount + 1);
        }

        waitlist = waitlistRepository.save(waitlist);
        return WaitlistResponse.fromWaitlist(waitlist);
    }

    public List<WaitlistResponse> getWaitlist(Long restaurantId, String status, String date) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        java.util.List<Waitlist> list;
        String dateParam = (date != null && !date.trim().isEmpty()) ? date.trim() : null;
        if (dateParam != null) {
            java.sql.Date sqlDate = java.sql.Date.valueOf(java.time.LocalDate.parse(dateParam));
            list = waitlistRepository.findByRestaurantIdAndJoinedDate(restaurantId, sqlDate);
        } else {
            java.sql.Date sqlDate = java.sql.Date.valueOf(java.time.LocalDate.now());
            list = waitlistRepository.findByRestaurantIdAndJoinedDate(restaurantId, sqlDate);
        }
        if (status != null && !status.trim().isEmpty()) {
            Waitlist.WaitlistStatus st = Waitlist.WaitlistStatus.valueOf(status.trim().toUpperCase());
            list = list.stream().filter(w -> w.getStatus() == st).collect(Collectors.toList());
        }
        return list.stream()
                .map(WaitlistResponse::fromWaitlist)
                .collect(Collectors.toList());
    }

    public WaitlistResponse notifyGuest(Long restaurantId, Long waitlistId, com.restaurant.waitlist.backend.dto.request.NotifyGuestRequest request) {
        Waitlist waitlist = waitlistService.getWaitlistById(restaurantId, waitlistId);
        if (request.getEstimatedWaitTime() != null) {
            waitlist.setEstimatedWaitTime(request.getEstimatedWaitTime());
        }
        waitlist.setStatus(Waitlist.WaitlistStatus.NOTIFIED);
        waitlistRepository.save(waitlist);
        String estimatedTime = waitlist.getEstimatedWaitTime() != null ? waitlist.getEstimatedWaitTime().toString() : "Soon";

        Integer position = waitlist.getPosition() !=null ? waitlist.getPosition() : null;
        smsService.sendWaitlistNotificationSms(
                waitlist.getGuestPhone(),
                waitlist.getGuestName(),
                estimatedTime,
                position
        );
        return WaitlistResponse.fromWaitlist(waitlist);
    }

    @org.springframework.transaction.annotation.Transactional
    public WaitlistResponse seatGuest(Long restaurantId, Long waitlistId) {
        Waitlist waitlist = waitlistService.getWaitlistById(restaurantId, waitlistId);
        if (waitlist.getRestaurant() == null || !waitlist.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Waitlist entry does not belong to the specified restaurant");
        }
        if (waitlist.getStatus() == Waitlist.WaitlistStatus.CANCELLED) {
            throw new RuntimeException("Waitlist entry was already cancelled");
        }
        if (waitlist.getStatus() == Waitlist.WaitlistStatus.SEATED) {
            throw new RuntimeException("Waitlist entry is already seated");
        }
        waitlist.setStatus(Waitlist.WaitlistStatus.SEATED);
        waitlist.setPosition(null);
        waitlist.setEstimatedWaitTime(null);
        waitlist.setSeatedAt(java.time.LocalDateTime.now());
        waitlistRepository.save(waitlist);
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<Waitlist> todaysActive = waitlistRepository.findByRestaurantIdAndJoinedDate(restaurantId, java.sql.Date.valueOf(today)).stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.WAITING || w.getStatus() == Waitlist.WaitlistStatus.NOTIFIED)
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
        int p = 1;
        for (Waitlist w : todaysActive) {
            w.setPosition(p++);
        }
        waitlistRepository.saveAll(todaysActive);
        return WaitlistResponse.fromWaitlist(waitlist);
    }

    @org.springframework.transaction.annotation.Transactional
    public WaitlistResponse approveGuest(Long restaurantId, Long waitlistId, com.restaurant.waitlist.backend.dto.request.NotifyGuestRequest request) {
        restaurantRepository.findById(restaurantId).orElseThrow(() -> new RuntimeException("Restaurant not found"));
        Waitlist waitlist = waitlistService.getWaitlistById(waitlistId);
        if (waitlist.getRestaurant() == null || !waitlist.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Waitlist entry does not belong to the specified restaurant");
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        long count = waitlistRepository.findByRestaurantIdAndJoinedDate(restaurantId, java.sql.Date.valueOf(today)).stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.WAITING || w.getStatus() == Waitlist.WaitlistStatus.NOTIFIED)
                .count();
        waitlist.setPosition((int) (count + 1));
        waitlist.setStatus(Waitlist.WaitlistStatus.WAITING);
        if (request.getEstimatedWaitTime() != null) {
            waitlist.setEstimatedWaitTime(request.getEstimatedWaitTime());
        }
        waitlistRepository.save(waitlist);
        String estimatedTime = waitlist.getEstimatedWaitTime() != null ? waitlist.getEstimatedWaitTime().toString() : "Soon";
        Integer position = waitlist.getPosition() != null ? waitlist.getPosition() : null;
        smsService.sendWaitlistNotificationSms(waitlist.getGuestPhone(), waitlist.getGuestName(), estimatedTime, position);
        return WaitlistResponse.fromWaitlist(waitlist);
    }

    @org.springframework.transaction.annotation.Transactional
    public void updateEstimate(Long restaurantId, Long waitlistId, com.restaurant.waitlist.backend.dto.request.NotifyGuestRequest request) {
        Waitlist waitlist = waitlistService.getWaitlistById(restaurantId, waitlistId);
        if (waitlist.getStatus() != Waitlist.WaitlistStatus.NOTIFIED) {
            throw new RuntimeException("Can only update estimate for notified entries");
        }
        if (request.getEstimatedWaitTime() != null) {
            waitlist.setEstimatedWaitTime(request.getEstimatedWaitTime());
        }
        waitlistRepository.save(waitlist);
        if (waitlist.getStatus() == Waitlist.WaitlistStatus.NOTIFIED && request.getEstimatedWaitTime() != null) {
            String message = "Hi " + waitlist.getGuestName() + ", your table at "
                    + waitlist.getRestaurant().getName()
                    + " is slightly delayed. New wait time is approximately "
                    + request.getEstimatedWaitTime() + " minutes. Thanks for your patience.";
            smsService.sendSms(waitlist.getGuestPhone(), message);
        }
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

        long seatedToday = allWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.SEATED &&
                        w.getSeatedAt() != null &&
                        w.getSeatedAt().isAfter(startOfDay) &&
                        w.getSeatedAt().isBefore(endOfDay))
                .count();

        long noShowsToday = todayWaitlists.stream()
                .filter(w -> w.getStatus() == Waitlist.WaitlistStatus.CANCELLED)
                .count();

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
                .totalNotified(0L)
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

