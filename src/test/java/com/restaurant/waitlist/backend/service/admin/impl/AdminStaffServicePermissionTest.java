package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.StaffPermissionRequest;
import com.restaurant.waitlist.backend.dto.response.admin.StaffPermissionResponse;
import com.restaurant.waitlist.backend.entity.Staff;
import com.restaurant.waitlist.backend.entity.StaffRole;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.StaffInvitationTokenRepository;
import com.restaurant.waitlist.backend.repository.StaffPermissionRepository;
import com.restaurant.waitlist.backend.repository.StaffRepository;
import com.restaurant.waitlist.backend.repository.UserRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStaffServicePermissionTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private StaffInvitationTokenRepository staffInvitationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StaffPermissionRepository staffPermissionRepository;

    @Mock
    private AdminLocationAccessService adminLocationAccessService;

    private AdminStaffServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminStaffServiceImpl(
                staffRepository,
                restaurantRepository,
                emailService,
                auditLogRepository,
                staffInvitationTokenRepository,
                userRepository,
                staffPermissionRepository,
                adminLocationAccessService
        );
    }

    @Test
    void getStaffPermissions_shouldUseDefaultRolePermissionsWhenNoOverrideExists() {
        Staff staff = Staff.builder()
                .id(10L)
                .name("Manager User")
                .role(StaffRole.MANAGER)
                .build();

        when(staffRepository.findById(10L)).thenReturn(Optional.of(staff));

        StaffPermissionResponse permissions = service.getStaffPermissions(10L);

        assertEquals(10L, permissions.getStaffId());
        assertEquals("MANAGER", permissions.getRole());
        assertEquals(true, permissions.getCanManageOffers());
        assertEquals(false, permissions.getCanManageStaff());
        assertEquals(true, permissions.getCanViewReports());
        assertEquals(false, permissions.getCanManageSettings());
        assertEquals(true, permissions.getCanManageRewards());
    }

    @Test
    void updateStaffPermissions_shouldPersistCustomPermissionsForStaff() {
        Staff staff = Staff.builder()
                .id(20L)
                .name("Host User")
                .role(StaffRole.HOST)
                .build();

        when(staffRepository.findById(20L)).thenReturn(Optional.of(staff));
        when(staffPermissionRepository.findByStaffId(20L)).thenReturn(Optional.empty());
        when(staffPermissionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StaffPermissionRequest update = StaffPermissionRequest.builder()
                .canManageOffers(true)
                .canManageStaff(false)
                .canViewReports(true)
                .canManageSettings(false)
                .canManageRewards(true)
                .build();

        when(staffPermissionRepository.findByStaffId(20L)).thenReturn(Optional.of(com.restaurant.waitlist.backend.entity.StaffPermission.builder()
                .staff(staff)
                .canManageOffers(true)
                .canManageStaff(false)
                .canViewReports(true)
                .canManageSettings(false)
                .canManageRewards(true)
                .custom(true)
                .build()));

        StaffPermissionResponse result = service.updateStaffPermissions(20L, update);

        assertEquals(20L, result.getStaffId());
        assertEquals("HOST", result.getRole());
        assertEquals(true, result.getCanManageOffers());
        assertEquals(true, result.getCanViewReports());
        assertEquals(true, result.getCanManageRewards());
    }
}
