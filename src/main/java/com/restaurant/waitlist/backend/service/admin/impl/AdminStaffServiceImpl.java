package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.AdminStaffRequest;
import com.restaurant.waitlist.backend.dto.response.admin.AdminStaffResponse;
import com.restaurant.waitlist.backend.entity.AuditLog;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Staff;
import com.restaurant.waitlist.backend.mapper.AdminLocationMapper;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.StaffRepository;
import com.restaurant.waitlist.backend.service.EmailService;
import com.restaurant.waitlist.backend.service.admin.AdminStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStaffServiceImpl implements AdminStaffService {

    private final StaffRepository staffRepository;
    private final RestaurantRepository restaurantRepository;
    private final EmailService emailService;
    private final AuditLogRepository auditLogRepository;

    @Override
    public Object listStaff(Pageable pageable) {
        Page<Staff> page = staffRepository.findAll(pageable);
        return page.map(s -> AdminStaffResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .role(s.getRole())
                .email(s.getEmail())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .locationId(s.getRestaurant() != null ? s.getRestaurant().getId() : null)
                .location(s.getRestaurant() != null ? s.getRestaurant().getName() : null)
                .build());
    }

    @Override
    @Transactional
    public AdminStaffResponse inviteStaff(AdminStaffRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // check duplicate email among existing staff
        boolean exists = staffRepository.findAll().stream()
                .anyMatch(s -> request.getEmail().equalsIgnoreCase(s.getEmail()));
        if (exists) throw new RuntimeException("Email already invited");

        Staff staff = Staff.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .role(request.getRole())
                .email(request.getEmail())
                .status(Staff.StaffStatus.INVITED)
                .build();

        Staff saved = staffRepository.save(staff);

        // send invitation email
        try {
            String body = "You have been invited to join Dinerly as " + request.getRole() + " for " + restaurant.getName();
            emailService.sendVerificationEmail(request.getEmail(), "invite-token-placeholder");
        } catch (Exception e) {
            // log and continue
        }

        AuditLog log = AuditLog.builder()
                .restaurantId(restaurant.getId())
                .action("STAFF_INVITED")
                .details("Invited " + saved.getName() + " (" + saved.getEmail() + ")")
                .build();
        auditLogRepository.save(log);

        return AdminStaffResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .role(saved.getRole())
                .email(saved.getEmail())
                .status(saved.getStatus().name())
                .locationId(restaurant.getId())
                .location(restaurant.getName())
                .build();
    }
}
