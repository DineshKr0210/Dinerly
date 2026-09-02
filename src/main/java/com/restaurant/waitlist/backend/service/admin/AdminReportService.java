package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminReportService {
    ReportResponse generateReport(String type, Long locationId, String period) throws Exception;
    Page<ReportResponse> listReports(Pageable pageable);
    byte[] downloadReport(Long reportId) throws Exception;
    ReportResponse getReportMetadata(Long reportId) throws Exception;
}
