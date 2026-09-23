package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.AdminStaffRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffPermissionRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffSetPasswordRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffUpdateRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffVerifyInvitationRequest;
import com.restaurant.waitlist.backend.dto.response.admin.AdminStaffResponse;
import com.restaurant.waitlist.backend.dto.response.admin.StaffPermissionResponse;
import com.restaurant.waitlist.backend.dto.response.admin.StaffTokenVerificationResponse;
import com.restaurant.waitlist.backend.entity.AuditLog;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Staff;
import com.restaurant.waitlist.backend.entity.StaffInvitationToken;
import com.restaurant.waitlist.backend.entity.StaffPermission;
import com.restaurant.waitlist.backend.entity.StaffRole;
import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.mapper.AdminLocationMapper;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.StaffInvitationTokenRepository;
import com.restaurant.waitlist.backend.repository.StaffPermissionRepository;
import com.restaurant.waitlist.backend.repository.StaffRepository;
import com.restaurant.waitlist.backend.repository.UserRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.AuditLogService;
import com.restaurant.waitlist.backend.service.EmailService;
import com.restaurant.waitlist.backend.service.admin.AdminStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final StaffInvitationTokenRepository staffInvitationTokenRepository;
    private final UserRepository userRepository;
    private final StaffPermissionRepository staffPermissionRepository;
    private final AdminLocationAccessService adminLocationAccessService;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Object listStaff(Pageable pageable) {
        Page<Staff> page = staffRepository.findByRestaurantIdIn(adminLocationAccessService.getAccessibleRestaurantIds(), pageable);
        return page.map(this::toResponse);
    }

    private AdminStaffResponse toResponse(Staff s) {
        return AdminStaffResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .role(s.getRole().name())
                .email(s.getEmail())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .locationId(s.getRestaurant() != null ? s.getRestaurant().getId() : null)
                .location(s.getRestaurant() != null ? s.getRestaurant().getName() : null)
                .build();
    }

    @Override
    @Transactional
    public AdminStaffResponse inviteStaff(AdminStaffRequest request) {
        adminLocationAccessService.assertAccess(request.getLocationId());
        Restaurant restaurant = restaurantRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        boolean exists = staffRepository.findAll().stream()
                .anyMatch(s -> request.getEmail().equalsIgnoreCase(s.getEmail()));
        if (exists) throw new RuntimeException("Email already invited");

        // Convert and validate role
        StaffRole staffRole = StaffRole.fromString(request.getRole());

        Staff staff = Staff.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .role(staffRole)
                .email(request.getEmail())
                .status(Staff.StaffStatus.INVITED)
                .build();

        Staff saved = staffRepository.save(staff);

        // Generate invitation token (valid for 24 hours)
        String invitationToken = StaffInvitationToken.generateToken();
        StaffInvitationToken token = StaffInvitationToken.builder()
                .staff(saved)
                .token(invitationToken)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .isUsed(false)
                .build();
        staffInvitationTokenRepository.save(token);

        // Send invitation email with proper token
        try {
            emailService.sendStaffInvitationEmail(
                    request.getEmail(),
                    request.getName(),
                    restaurant.getName(),
                    invitationToken
            );
        } catch (Exception e) {
            // Log error but don't fail the invitation
            System.err.println("Failed to send invitation email: " + e.getMessage());
        }

        auditLogService.log(restaurant.getId(), "STAFF_INVITED",
                "Invited " + saved.getName() + " (" + saved.getEmail() + ") as " + saved.getRole().name());

        return toResponse(saved);
    }

    @Override
    public AdminStaffResponse getStaffById(Long staffId) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        adminLocationAccessService.assertAccess(s.getRestaurant() != null ? s.getRestaurant().getId() : null);
        return toResponse(s);
    }

    @Override
    @Transactional
    public AdminStaffResponse updateStaff(Long staffId, StaffUpdateRequest request) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        adminLocationAccessService.assertAccess(s.getRestaurant() != null ? s.getRestaurant().getId() : null);
        adminLocationAccessService.assertAccess(request.getLocationId());

        if (request.getName() != null) s.setName(request.getName());
        if (request.getRole() != null) {
            s.setRole(StaffRole.fromString(request.getRole()));
        }
        if (request.getEmail() != null) s.setEmail(request.getEmail());
        if (request.getLocationId() != null) {
            Restaurant r = restaurantRepository.findById(request.getLocationId()).orElseThrow(() -> new RuntimeException("Restaurant not found"));
            s.setRestaurant(r);
        }
        if (request.getStatus() != null) {
            s.setStatus(Staff.StaffStatus.valueOf(request.getStatus().toUpperCase()));
        }
        
        Staff saved = staffRepository.save(s);

        auditLogService.log(s.getRestaurant() != null ? s.getRestaurant().getId() : 0L, "STAFF_UPDATED", "Updated staff: " + saved.getName());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deactivateStaff(Long staffId) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        adminLocationAccessService.assertAccess(s.getRestaurant() != null ? s.getRestaurant().getId() : null);
        s.setStatus(Staff.StaffStatus.INACTIVE);
        staffRepository.save(s);

        auditLogService.log(s.getRestaurant() != null ? s.getRestaurant().getId() : 0L, "STAFF_DEACTIVATED", "Deactivated staff: " + s.getName());
    }

    @Override
    @Transactional
    public void activateStaff(Long staffId) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        adminLocationAccessService.assertAccess(s.getRestaurant() != null ? s.getRestaurant().getId() : null);
        s.setStatus(Staff.StaffStatus.ACTIVE);
        staffRepository.save(s);

        auditLogService.log(s.getRestaurant() != null ? s.getRestaurant().getId() : 0L, "STAFF_ACTIVATED", "Activated staff: " + s.getName());
    }

    @Override
    public StaffPermissionResponse getStaffPermissions(Long staffId) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        adminLocationAccessService.assertAccess(s.getRestaurant() != null ? s.getRestaurant().getId() : null);
        Map<String, Boolean> effective = getEffectivePermissions(staffId);
        StaffPermission existing = staffPermissionRepository.findByStaffId(staffId).orElse(null);

        return StaffPermissionResponse.builder()
                .staffId(s.getId())
                .role(s.getRole().name())
                .canManageOffers(effective.get("canManageOffers"))
                .canManageStaff(effective.get("canManageStaff"))
                .canViewReports(effective.get("canViewReports"))
                .canManageSettings(effective.get("canManageSettings"))
                .canManageRewards(effective.get("canManageRewards"))
                .effectivePermissions(effective)
                .isCustom(existing != null && existing.isCustom())
                .updatedAt(existing != null ? existing.getUpdatedAt() : null)
                .build();
    }

    @Override
    @Transactional
    public StaffPermissionResponse updateStaffPermissions(Long staffId, StaffPermissionRequest request) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        adminLocationAccessService.assertAccess(s.getRestaurant() != null ? s.getRestaurant().getId() : null);

        if (request == null) {
            throw new RuntimeException("Permission payload is required");
        }

        StaffPermission staffPermission = staffPermissionRepository.findByStaffId(staffId)
                .orElseGet(() -> StaffPermission.builder().staff(s).build());

        staffPermission.setCanManageOffers(Boolean.TRUE.equals(request.getCanManageOffers()));
        staffPermission.setCanManageStaff(Boolean.TRUE.equals(request.getCanManageStaff()));
        staffPermission.setCanViewReports(Boolean.TRUE.equals(request.getCanViewReports()));
        staffPermission.setCanManageSettings(Boolean.TRUE.equals(request.getCanManageSettings()));
        staffPermission.setCanManageRewards(Boolean.TRUE.equals(request.getCanManageRewards()));
        staffPermission.setCustom(true);
        staffPermissionRepository.save(staffPermission);

        auditLogService.log(s.getRestaurant() != null ? s.getRestaurant().getId() : 0L, "STAFF_PERMISSIONS_UPDATED", "Updated permissions for staff: " + s.getName());

        return getStaffPermissions(staffId);
    }

    @Override
    public Map<String, Boolean> getEffectivePermissions(Long staffId) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        Map<String, Boolean> defaultValues = getDefaultPermissionsByRole(s.getRole());

        return staffPermissionRepository.findByStaffId(staffId)
                .map(permission -> {
                    Map<String, Boolean> result = new HashMap<>();
                    result.put("canManageOffers", permission.isCanManageOffers());
                    result.put("canManageStaff", permission.isCanManageStaff());
                    result.put("canViewReports", permission.isCanViewReports());
                    result.put("canManageSettings", permission.isCanManageSettings());
                    result.put("canManageRewards", permission.isCanManageRewards());
                    return result;
                })
                .orElse(defaultValues);
    }

    private Map<String, Boolean> getDefaultPermissionsByRole(StaffRole role) {
        Map<String, Boolean> permissions = new HashMap<>();

        if (role == StaffRole.ADMIN) {
            permissions.put("canManageOffers", true);
            permissions.put("canManageStaff", true);
            permissions.put("canViewReports", true);
            permissions.put("canManageSettings", true);
            permissions.put("canManageRewards", true);
        } else if (role == StaffRole.MANAGER) {
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
    public Page<Map<String, Object>> getStaffActivityLog(Long staffId, Pageable pageable) {
        Staff s = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        adminLocationAccessService.assertAccess(s.getRestaurant() != null ? s.getRestaurant().getId() : null);

        return buildActivityLogPage(
                l -> "STAFF_INVITED".equals(l.getAction()) || "STAFF_UPDATED".equals(l.getAction())
                        || "STAFF_DEACTIVATED".equals(l.getAction()) || "STAFF_ACTIVATED".equals(l.getAction()),
                false,
                pageable);
    }

    @Override
    public Page<Map<String, Object>> getAllStaffActivityLog(Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.getAccessibleRestaurantIds();
        return buildActivityLogPage(
                l -> l.getAction().contains("STAFF") && restaurantIds.contains(l.getRestaurantId()),
                true,
                pageable);
    }

    private Page<Map<String, Object>> buildActivityLogPage(java.util.function.Predicate<AuditLog> filter,
                                                             boolean includeRestaurantId, Pageable pageable) {
        List<AuditLog> logs = auditLogRepository.findAll().stream()
                .filter(filter)
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());

        List<Map<String, Object>> activities = logs.stream()
                .map(log -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", log.getId());
                    activity.put("action", log.getAction());
                    activity.put("details", log.getDetails());
                    if (includeRestaurantId) {
                        activity.put("restaurantId", log.getRestaurantId());
                    }
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
    public StaffTokenVerificationResponse verifyInvitationToken(String token) {
        StaffInvitationToken invitationToken = staffInvitationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

        if (invitationToken.getIsUsed()) {
            throw new RuntimeException("This invitation has already been used");
        }

        if (!invitationToken.isValid()) {
            throw new RuntimeException("This invitation token has expired");
        }

        Staff staff = invitationToken.getStaff();
        Restaurant restaurant = staff.getRestaurant();

        return StaffTokenVerificationResponse.builder()
                .valid(true)
                .message("Invitation token is valid. Please set your password.")
                .staffId(staff.getId())
                .staffName(staff.getName())
                .staffEmail(staff.getEmail())
                .restaurantName(restaurant.getName())
                .build();
    }

    /**
     * Set password for staff after accepting invitation
     * Creates corresponding User record for authentication
     */
    @Override
    @Transactional
    public AdminStaffResponse setStaffPassword(StaffSetPasswordRequest request) {
        StaffInvitationToken invitationToken = staffInvitationTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

        if (invitationToken.getIsUsed()) {
            throw new RuntimeException("This invitation has already been used");
        }

        if (!invitationToken.isValid()) {
            throw new RuntimeException("This invitation token has expired");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }

        Staff staff = invitationToken.getStaff();

        // Encrypt and set password
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        staff.setPassword(encryptedPassword);
        staff.setStatus(Staff.StaffStatus.ACTIVE);

        Staff savedStaff = staffRepository.save(staff);

        // ✅ NEW: Create User record for authentication
        User user = User.builder()
                .email(staff.getEmail())
                .password(encryptedPassword)
                .name(staff.getName())
                .phone(null)
                .role(mapStaffRoleToUserRole(staff.getRole()))
                .restaurantId(staff.getRestaurant().getId())
                .staffId(staff.getId())
                .emailVerified(true)
                .enabled(true)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // ✅ Link User back to Staff
        savedStaff.setUserId(savedUser.getId());
        staffRepository.save(savedStaff);

        // Mark token as used
        invitationToken.setIsUsed(true);
        staffInvitationTokenRepository.save(invitationToken);

        // Log the action
        auditLogService.log(staff.getRestaurant().getId(), "STAFF_ACTIVATED_VIA_INVITATION",
                "Staff " + staff.getName() + " (" + staff.getEmail() + ") activated with role: " + staff.getRole().name());

        return toResponse(savedStaff);
    }

    /**
     * Maps StaffRole to the corresponding User.UserRole for authentication purposes.
     */
    private User.UserRole mapStaffRoleToUserRole(StaffRole staffRole) {
        return switch (staffRole) {
            case ADMIN -> User.UserRole.ADMIN;
            case MANAGER -> User.UserRole.MANAGER;
            case HOST -> User.UserRole.HOST;
        };
    }
}
