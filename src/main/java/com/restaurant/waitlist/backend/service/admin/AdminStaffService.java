package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.AdminStaffRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffUpdateRequest;
import com.restaurant.waitlist.backend.dto.response.admin.AdminStaffResponse;
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
    Map<String, Object> getStaffPermissions(Long staffId);
    Map<String, Object> updateStaffPermissions(Long staffId, Map<String, Boolean> permissions);
    
    // Activity and audit
    Page<Map<String, Object>> getStaffActivityLog(Long staffId, Pageable pageable);
    Page<Map<String, Object>> getAllStaffActivityLog(Pageable pageable);
}

