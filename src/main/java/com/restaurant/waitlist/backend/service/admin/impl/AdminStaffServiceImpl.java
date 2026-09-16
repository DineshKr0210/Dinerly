package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.AdminStaffRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffUpdateRequest;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public AdminStaffResponse getStaffById(Long staffId) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        return AdminStaffResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .role(s.getRole())
                .email(s.getEmail())
                .status(s.getStatus().name())
                .locationId(s.getRestaurant() != null ? s.getRestaurant().getId() : null)
                .location(s.getRestaurant() != null ? s.getRestaurant().getName() : null)
                .build();
    }

    @Override
    @Transactional
    public AdminStaffResponse updateStaff(Long staffId, StaffUpdateRequest request) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        
        if (request.getName() != null) s.setName(request.getName());
        if (request.getRole() != null) s.setRole(request.getRole());
        if (request.getEmail() != null) s.setEmail(request.getEmail());
        if (request.getLocationId() != null) {
            Restaurant r = restaurantRepository.findById(request.getLocationId()).orElseThrow(() -> new RuntimeException("Restaurant not found"));
            s.setRestaurant(r);
        }
        if (request.getStatus() != null) {
            s.setStatus(Staff.StaffStatus.valueOf(request.getStatus().toUpperCase()));
        }
        
        Staff saved = staffRepository.save(s);
        
        AuditLog log = AuditLog.builder()
                .restaurantId(s.getRestaurant() != null ? s.getRestaurant().getId() : 0L)
                .action("STAFF_UPDATED")
                .details("Updated staff: " + saved.getName())
                .build();
        auditLogRepository.save(log);
        
        return AdminStaffResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .role(saved.getRole())
                .email(saved.getEmail())
                .status(saved.getStatus().name())
                .locationId(saved.getRestaurant() != null ? saved.getRestaurant().getId() : null)
                .location(saved.getRestaurant() != null ? saved.getRestaurant().getName() : null)
                .build();
    }

    @Override
    @Transactional
    public void deactivateStaff(Long staffId) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        s.setStatus(Staff.StaffStatus.INACTIVE);
        staffRepository.save(s);
        
        AuditLog log = AuditLog.builder()
                .restaurantId(s.getRestaurant() != null ? s.getRestaurant().getId() : 0L)
                .action("STAFF_DEACTIVATED")
                .details("Deactivated staff: " + s.getName())
                .build();
        auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public void activateStaff(Long staffId) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        s.setStatus(Staff.StaffStatus.ACTIVE);
        staffRepository.save(s);
        
        AuditLog log = AuditLog.builder()
                .restaurantId(s.getRestaurant() != null ? s.getRestaurant().getId() : 0L)
                .action("STAFF_ACTIVATED")
                .details("Activated staff: " + s.getName())
                .build();
        auditLogRepository.save(log);
    }

    @Override
    public Map<String, Object> getStaffPermissions(Long staffId) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("staffId", s.getId());
        permissions.put("role", s.getRole());
        
        // Role-based permissions
        if ("Owner".equalsIgnoreCase(s.getRole())) {
            permissions.put("canManageOffers", true);
            permissions.put("canManageStaff", true);
            permissions.put("canViewReports", true);
            permissions.put("canManageSettings", true);
            permissions.put("canManageRewards", true);
        } else if ("Manager".equalsIgnoreCase(s.getRole())) {
            permissions.put("canManageOffers", true);
            permissions.put("canManageStaff", false);
            permissions.put("canViewReports", true);
            permissions.put("canManageSettings", false);
            permissions.put("canManageRewards", true);
        } else {
            permissions.put("canManageOffers", false);
            permissions.put("canManageStaff", false);
            permissions.put("canViewReports", false);
            permissions.put("canManageSettings", false);
            permissions.put("canManageRewards", false);
        }
        
        return permissions;
    }

    @Override
    @Transactional
    public Map<String, Object> updateStaffPermissions(Long staffId, Map<String, Boolean> permissions) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        
        AuditLog log = AuditLog.builder()
                .restaurantId(s.getRestaurant() != null ? s.getRestaurant().getId() : 0L)
                .action("STAFF_PERMISSIONS_UPDATED")
                .details("Updated permissions for staff: " + s.getName())
                .build();
        auditLogRepository.save(log);
        
        return getStaffPermissions(staffId);
    }

    @Override
    public Page<Map<String, Object>> getStaffActivityLog(Long staffId, Pageable pageable) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        
        List<AuditLog> logs = auditLogRepository.findAll().stream()
                .filter(l -> "STAFF_INVITED".equals(l.getAction()) || "STAFF_UPDATED".equals(l.getAction()) 
                        || "STAFF_DEACTIVATED".equals(l.getAction()) || "STAFF_ACTIVATED".equals(l.getAction()))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
        
        List<Map<String, Object>> activities = logs.stream()
                .map(log -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", log.getId());
                    activity.put("action", log.getAction());
                    activity.put("details", log.getDetails());
                    activity.put("timestamp", log.getId()); // Use ID as timestamp placeholder
                    return activity;
                })
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), activities.size());
        
        return new PageImpl<>(
            activities.subList(start, end),
            pageable,
            activities.size()
        );
    }

    @Override
    public Page<Map<String, Object>> getAllStaffActivityLog(Pageable pageable) {
        List<AuditLog> logs = auditLogRepository.findAll().stream()
                .filter(l -> l.getAction().contains("STAFF"))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
        
        List<Map<String, Object>> activities = logs.stream()
                .map(log -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", log.getId());
                    activity.put("action", log.getAction());
                    activity.put("details", log.getDetails());
                    activity.put("restaurantId", log.getRestaurantId());
                    activity.put("timestamp", log.getId());
                    return activity;
                })
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), activities.size());
        
        return new PageImpl<>(
            activities.subList(start, end),
            pageable,
            activities.size()
        );
    }
}

