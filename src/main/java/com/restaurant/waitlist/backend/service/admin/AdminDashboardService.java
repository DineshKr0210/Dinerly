package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.AdminDashboardResponse;
import java.time.LocalDate;

public interface AdminDashboardService {
    AdminDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, int topN, Long locationId);
}
