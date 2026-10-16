package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.StaffResponse;
import com.restaurant.waitlist.backend.entity.Staff;
import com.restaurant.waitlist.backend.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    public List<StaffResponse> getStaff(Long restaurantId) {
        return staffRepository.findByRestaurantId(restaurantId).stream()
                .map(StaffResponse::fromStaff)
                .collect(Collectors.toList());
    }

    /**
     * Get a staff member by ID.
     * 
     * @param staffId The staff ID to retrieve
     * @return Staff entity
     * @throws RuntimeException if staff not found
     */
    public Staff getStaffById(Long staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff member not found: " + staffId));
    }
}
