package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.AdminStaffRequest;
import com.restaurant.waitlist.backend.dto.response.admin.AdminStaffResponse;
import org.springframework.data.domain.Pageable;

public interface AdminStaffService {
    Object listStaff(Pageable pageable);
    AdminStaffResponse inviteStaff(AdminStaffRequest request);
}
