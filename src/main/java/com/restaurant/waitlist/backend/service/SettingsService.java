package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.SettingsProfileResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class SettingsService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private SmsService smsService;

    public SettingsProfileResponse getProfileSettings(Long restaurantId, Integer year, Integer month) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        int targetYear = year != null ? year : LocalDate.now().getYear();
        int targetMonth = month != null ? month : LocalDate.now().getMonthValue();

        long smsSentThisMonth = waitlistRepository.countSentSmsThisMonth(restaurantId, targetYear, targetMonth);
        double smsChargesThisMonth = smsService.getCurrentMonthEstimatedCharge("sms");
        double callChargesThisMonth = smsService.getCurrentMonthEstimatedCharge("call");
        double totalChargesThisMonth = smsChargesThisMonth + callChargesThisMonth;;

        YearMonth renewalMonth = YearMonth.of(targetYear, targetMonth).plusMonths(1);

        SettingsProfileResponse.ProfileResponse profileResponse = SettingsProfileResponse.ProfileResponse.builder()
                .restaurant(SettingsProfileResponse.RestaurantProfileResponse.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .email(restaurant.getEmail())
                        .phone(restaurant.getPhone())
                        .address(restaurant.getAddress())
                        .hours(SettingsProfileResponse.HoursResponse.builder()
                                .open(restaurant.getOpenTime())
                                .close(restaurant.getCloseTime())
                                .build())
                        .build())
                .plan(SettingsProfileResponse.PlanResponse.builder()
                        .name("Basic")
                        .smssentthismonth((int) smsSentThisMonth)
                        .marketingsmssentthismonth(0)
                        .smsChargesThisMonth(smsChargesThisMonth)
                        .callChargesThisMonth(callChargesThisMonth)
                        .totalChargesThisMonth(totalChargesThisMonth)
                        .nextRenewal(renewalMonth.atDay(1).toString())
                        .build())
                .build();

        return SettingsProfileResponse.builder().profile(profileResponse).build();
    }

    public SettingsProfileResponse updateProfileSettings(Long restaurantId, com.restaurant.waitlist.backend.dto.request.UpdateSettingsProfileRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getEmail() != null) {
            restaurant.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            restaurant.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
        }
        if (request.getHours() != null) {
            if (request.getHours().getOpen() != null) {
                restaurant.setOpenTime(request.getHours().getOpen());
            }
            if (request.getHours().getClose() != null) {
                restaurant.setCloseTime(request.getHours().getClose());
            }
        }

        restaurantRepository.save(restaurant);
        return getProfileSettings(restaurantId, null, null);
    }
}
