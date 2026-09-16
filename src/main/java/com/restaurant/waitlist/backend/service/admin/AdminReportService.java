package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.ReportScheduleRequest;
import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;

public interface AdminReportService {
    ReportResponse generateReport(String type, Long locationId, String period) throws Exception;
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

