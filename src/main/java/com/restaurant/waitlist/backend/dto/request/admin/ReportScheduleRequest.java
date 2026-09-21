package com.restaurant.waitlist.backend.dto.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportScheduleRequest {
    private String name;

    @Schema(description = "Configured report type", allowableValues = {"overall", "location", "performance", "redemption", "customer"})
    private String type; // overall, location

    private Long locationId;
    private String period; // last7days, last30days, last3months
    private String frequency; // daily, weekly, monthly
    private LocalDateTime scheduleTime;
    private String[] emailRecipients;
    private String exportFormat; // csv, pdf, excel
}
