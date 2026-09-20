package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.ReportScheduleRequest;
import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.Map;

public interface AdminReportService {
    /**
     * Generate a report with optional custom date range.
     * @param type "overall" (all locations) or "location" (single location)
     * @param locationId Required if type="location", ignored if type="overall"
     * @param period Preset period (pastMonth, pastWeek, etc.) - used if startDate/endDate not provided
     * @param startDate Custom start date (ISO 8601) - takes precedence over period
     * @param endDate Custom end date (ISO 8601) - takes precedence over period
     */
    ReportResponse generateReport(String type, Long locationId, String period, LocalDate startDate, LocalDate endDate) throws Exception;
    Page<ReportResponse> listReports(Pageable pageable);
    byte[] downloadReport(Long reportId) throws Exception;
    ReportResponse getReportMetadata(Long reportId) throws Exception;
    
    // Report export formats
    byte[] exportReportAsExcel(Long reportId) throws Exception;
    byte[] exportReportAsPdf(Long reportId) throws Exception;
    byte[] exportReportCsv(Long reportId) throws Exception;
    
    // Report scheduling
    Map<String, Object> scheduleReport(ReportScheduleRequest request);
    Page<Map<String, Object>> listScheduledReports(Pageable pageable);
    void cancelScheduledReport(Long scheduleId);
    
    // Custom report builder
    Map<String, Object> createCustomReportTemplate(Map<String, Object> config);
    Page<Map<String, Object>> listCustomReportTemplates(Pageable pageable);
}

