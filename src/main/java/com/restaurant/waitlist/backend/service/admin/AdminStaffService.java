package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.AdminStaffRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffPermissionRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffSetPasswordRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffUpdateRequest;
import com.restaurant.waitlist.backend.dto.response.admin.AdminStaffResponse;
import com.restaurant.waitlist.backend.dto.response.admin.StaffPermissionResponse;
import com.restaurant.waitlist.backend.dto.response.admin.StaffTokenVerificationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.Map;
import java.util.List;

public interface AdminStaffService {
    Object listStaff(Pageable pageable);
    AdminStaffResponse inviteStaff(AdminStaffRequest request);
    
    // Additional CRUD operations
    AdminStaffResponse getStaffById(Long staffId);
    AdminStaffResponse updateStaff(Long staffId, StaffUpdateRequest request);
    void deactivateStaff(Long staffId);
    void activateStaff(Long staffId);
    
    // Permissions management
    StaffPermissionResponse getStaffPermissions(Long staffId);
    StaffPermissionResponse updateStaffPermissions(Long staffId, StaffPermissionRequest request);
    Map<String, Boolean> getEffectivePermissions(Long staffId);

    // Activity and audit
    Page<Map<String, Object>> getStaffActivityLog(Long staffId, Pageable pageable);
    Page<Map<String, Object>> getAllStaffActivityLog(Pageable pageable);

    // Invitation token management
    StaffTokenVerificationResponse verifyInvitationToken(String token);
    AdminStaffResponse setStaffPassword(StaffSetPasswordRequest request);
}

