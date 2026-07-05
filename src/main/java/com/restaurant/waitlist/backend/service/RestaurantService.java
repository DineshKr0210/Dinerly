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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.restaurant.waitlist.backend.dto.request.SeatGuestRequest;
import com.restaurant.waitlist.backend.dto.response.WaitlistSmsResult;
import com.restaurant.waitlist.backend.dto.response.ReportsResponse;
import com.restaurant.waitlist.backend.repository.spec.WaitlistSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

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
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .map(WaitlistResponse::fromWaitlist)
                .collect(Collectors.toList());
    }

    public WaitlistSmsResult notifyGuest(Long restaurantId, Long waitlistId, com.restaurant.waitlist.backend.dto.request.NotifyGuestRequest request) {
        Waitlist waitlist = waitlistService.getWaitlistById(restaurantId, waitlistId);

        if (request.getEstimatedWaitTime() != null) {
            waitlist.setEstimatedWaitTime(request.getEstimatedWaitTime());
        }

        // Transition to NOTIFIED and set notifiedAt only once
        if (waitlist.getStatus() != Waitlist.WaitlistStatus.NOTIFIED) {
            waitlist.setStatus(Waitlist.WaitlistStatus.NOTIFIED);
            if (waitlist.getNotifiedAt() == null) {
                waitlist.setNotifiedAt(java.time.LocalDateTime.now());
            }
        }

        waitlistRepository.save(waitlist);

        boolean smsSent = true;
        String smsError = null;
        String message = null;
        try {
            String estimatedTime = waitlist.getEstimatedWaitTime() != null ? waitlist.getEstimatedWaitTime().toString() : "Soon";
            Integer position = waitlist.getPosition() != null ? waitlist.getPosition() : null;
            message = smsService.sendWaitlistNotificationSms(waitlist.getGuestPhone(), waitlist.getGuestName(), estimatedTime, position);
            waitlist.setSmsMessage(message);
            waitlist.setSmsStatus("SENT");
            waitlist.setSmsSentAt(LocalDateTime.now());
            waitlist.setSmsError(null);
        } catch (Exception e) {
            smsSent = false;
            smsError = e.getMessage();
            log.error("SMS send failed for notifyGuest id={} restaurantId={} error={}", waitlistId, restaurantId, smsError);
            waitlist.setSmsStatus("FAILED");
            waitlist.setSmsError(smsError);
        }
        waitlistRepository.save(waitlist);

        return new WaitlistSmsResult(WaitlistResponse.fromWaitlist(waitlist), smsSent, smsError);
    }

    @org.springframework.transaction.annotation.Transactional
    public WaitlistResponse seatGuest(Long restaurantId, Long waitlistId, SeatGuestRequest seatReq) {
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
        if (waitlist.getSeatedAt() == null) {
            waitlist.setSeatedAt(java.time.LocalDateTime.now());
        }
        if (seatReq != null && seatReq.getTableName() != null && !seatReq.getTableName().isBlank() && waitlist.getTableName() == null) {
            waitlist.setTableName(seatReq.getTableName());
        }

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
    public WaitlistSmsResult approveGuest(Long restaurantId, Long waitlistId, com.restaurant.waitlist.backend.dto.request.NotifyGuestRequest request) {
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
        if (waitlist.getApprovedAt() == null) {
            waitlist.setApprovedAt(java.time.LocalDateTime.now());
        }
        waitlistRepository.save(waitlist);

        boolean smsSent = true;
        String smsError = null;
        String message = null;
        try {
            String estimatedTime = waitlist.getEstimatedWaitTime() != null ? waitlist.getEstimatedWaitTime().toString() : "Soon";
            Integer position = waitlist.getPosition() != null ? waitlist.getPosition() : null;
            message = smsService.sendApprovedNotificationSms(waitlist.getGuestPhone(), waitlist.getGuestName(), estimatedTime, position);
            waitlist.setSmsMessage(message);
            waitlist.setSmsStatus("SENT");
            waitlist.setSmsSentAt(LocalDateTime.now());
            waitlist.setSmsError(null);
        } catch (Exception e) {
            smsSent = false;
            smsError = e.getMessage();
            log.error("SMS send failed for approveGuest id={} restaurantId={} error={}", waitlistId, restaurantId, smsError);
            waitlist.setSmsStatus("FAILED");
            waitlist.setSmsError(smsError);
        }
        waitlistRepository.save(waitlist);

        return new WaitlistSmsResult(WaitlistResponse.fromWaitlist(waitlist), smsSent, smsError);
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
            try {
                smsService.sendSms(waitlist.getGuestPhone(), message);
                waitlist.setSmsMessage(message);
                waitlist.setSmsStatus("SENT");
                waitlist.setSmsSentAt(LocalDateTime.now());
                waitlist.setSmsError(null);
            } catch (Exception e) {
                log.error("SMS send failed for updateEstimate id={} restaurantId={} error={}", waitlistId, restaurantId, e.getMessage());
                waitlist.setSmsMessage(message);
                waitlist.setSmsStatus("FAILED");
                waitlist.setSmsError(e.getMessage());
            }
            waitlistRepository.save(waitlist);
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

