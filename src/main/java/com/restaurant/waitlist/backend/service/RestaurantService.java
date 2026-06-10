package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.AddGuestRequest;
import com.restaurant.waitlist.backend.dto.request.CreateRestaurantRequest;
import com.restaurant.waitlist.backend.dto.response.RestaurantResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

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
}

