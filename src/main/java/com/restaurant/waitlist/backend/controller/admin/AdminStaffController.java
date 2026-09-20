package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.AdminStaffRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffSetPasswordRequest;
import com.restaurant.waitlist.backend.dto.request.admin.StaffUpdateRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.AdminStaffResponse;
import com.restaurant.waitlist.backend.dto.response.admin.StaffTokenVerificationResponse;
import com.restaurant.waitlist.backend.service.admin.AdminStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Object resp = adminStaffService.listStaff(pageable);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminStaffResponse>> invite(@Valid @RequestBody AdminStaffRequest request) {
        AdminStaffResponse resp = adminStaffService.inviteStaff(request);
        return ResponseEntity.ok(ApiResponse.success("Staff invitation sent successfully", resp));
    }

    @GetMapping("/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminStaffResponse>> getStaff(@PathVariable Long staffId) {
        AdminStaffResponse resp = adminStaffService.getStaffById(staffId);
        return ResponseEntity.ok(ApiResponse.success("Staff retrieved", resp));
    }

    @PutMapping("/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminStaffResponse>> updateStaff(@PathVariable Long staffId, @Valid @RequestBody StaffUpdateRequest request) {
        AdminStaffResponse resp = adminStaffService.updateStaff(staffId, request);
        return ResponseEntity.ok(ApiResponse.success("Staff updated", resp));
    }

    @PatchMapping("/{staffId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deactivateStaff(@PathVariable Long staffId) {
        adminStaffService.deactivateStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success("Staff deactivated", null));
    }

    @PatchMapping("/{staffId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> activateStaff(@PathVariable Long staffId) {
        adminStaffService.activateStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success("Staff activated", null));
    }

    @GetMapping("/{staffId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStaffPermissions(@PathVariable Long staffId) {
        Map<String, Object> permissions = adminStaffService.getStaffPermissions(staffId);
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved", permissions));
    }

    @PutMapping("/{staffId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStaffPermissions(@PathVariable Long staffId, @RequestBody Map<String, Boolean> permissions) {
        Map<String, Object> updated = adminStaffService.updateStaffPermissions(staffId, permissions);
        return ResponseEntity.ok(ApiResponse.success("Permissions updated", updated));
    }

    @GetMapping("/{staffId}/activity-log")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getStaffActivityLog(@PathVariable Long staffId, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Activity log retrieved", 
            adminStaffService.getStaffActivityLog(staffId, pageable)));
    }

    @GetMapping("/activity-log")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllStaffActivityLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Activity log retrieved", 
            adminStaffService.getAllStaffActivityLog(pageable)));
    }

    /**
     * Verify staff invitation token - Public endpoint (no auth required)
     * Called when staff clicks the invitation link
     * Usage: GET /api/admin/staff/verify-invitation?token=<invitation-token>
     */
    @GetMapping("/verify-invitation")
    public ResponseEntity<ApiResponse<StaffTokenVerificationResponse>> verifyInvitationToken(
            @RequestParam String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invitation token is required"));
        }
        try {
            StaffTokenVerificationResponse response = adminStaffService.verifyInvitationToken(token);
            return ResponseEntity.ok(ApiResponse.success("Invitation token verified", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Set password for staff after accepting invitation - Public endpoint
     * Staff will call this after clicking the invitation link to create their password
     */
    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse<AdminStaffResponse>> setStaffPassword(
            @Valid @RequestBody StaffSetPasswordRequest request) {
        try {
            AdminStaffResponse response = adminStaffService.setStaffPassword(request);
            return ResponseEntity.ok(ApiResponse.success(
                    "Password set successfully. Your account is now active!", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
