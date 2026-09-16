package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportScheduleRequest {
    private String name;
    private String type; // overall, location
    private Long locationId;
    private String period; // last7days, last30days, last3months
    private String frequency; // daily, weekly, monthly
    private LocalDateTime scheduleTime;
    private String[] emailRecipients;
    private String exportFormat; // csv, pdf, excel
}
