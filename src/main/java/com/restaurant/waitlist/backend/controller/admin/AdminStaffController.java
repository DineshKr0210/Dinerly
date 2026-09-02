package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.AdminStaffRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.AdminStaffResponse;
import com.restaurant.waitlist.backend.service.admin.AdminStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Object resp = adminStaffService.listStaff(pageable);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminStaffResponse>> invite(@Valid @RequestBody AdminStaffRequest request) {
        AdminStaffResponse resp = adminStaffService.inviteStaff(request);
        return ResponseEntity.ok(ApiResponse.success("Staff invitation sent successfully", resp));
    }
}
