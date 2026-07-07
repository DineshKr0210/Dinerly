package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.CreateStaffRequest;
import com.restaurant.waitlist.backend.dto.response.StaffResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Staff;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private StaffRepository staffRepository;

    public StaffResponse createStaff(Long restaurantId, CreateStaffRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        Staff staff = Staff.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .role(request.getRole())
                .build();
        return StaffResponse.fromStaff(staffRepository.save(staff));
    }

    public List<StaffResponse> getStaff(Long restaurantId) {
        return staffRepository.findByRestaurantId(restaurantId).stream()
                .map(StaffResponse::fromStaff)
                .collect(Collectors.toList());
    }

    public void deleteStaff(Long restaurantId, Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        if (!staff.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Staff does not belong to restaurant");
        }
        staffRepository.delete(staff);
    }
}
