package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.UpdateSmsTemplateRequest;
import com.restaurant.waitlist.backend.dto.request.CreateSmsTemplateRequest;
import com.restaurant.waitlist.backend.dto.request.admin.MarketingCampaignRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.SmsTemplateResponse;
import com.restaurant.waitlist.backend.dto.response.admin.MarketingSummaryResponse;
import com.restaurant.waitlist.backend.service.admin.AdminCampaignService;
import com.restaurant.waitlist.backend.service.admin.AdminMarketingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/marketing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Marketing", description = "SMS templates and one-off marketing sends")
public class AdminMarketingController {

    private final AdminMarketingService adminMarketingService;
    private final AdminCampaignService adminCampaignService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MarketingSummaryResponse>> getSummary(@RequestParam(required = false) Long locationId) {
        MarketingSummaryResponse response = adminCampaignService.getMarketingSummary(locationId);
        return ResponseEntity.ok(ApiResponse.success("Marketing summary retrieved", response));
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<SmsTemplateResponse>>> listTemplates() {
        List<SmsTemplateResponse> list = adminMarketingService.listTemplates();
        return ResponseEntity.ok(ApiResponse.success("Templates retrieved", list));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<SmsTemplateResponse>> updateTemplate(@PathVariable Long id,
                                                                           @RequestBody UpdateSmsTemplateRequest request) {
        SmsTemplateResponse resp = adminMarketingService.updateTemplate(id, request.getMessageTemplate(), request.getDescription());
        return ResponseEntity.ok(ApiResponse.success("Template updated", resp));
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<SmsTemplateResponse>> createTemplate(@RequestBody CreateSmsTemplateRequest request) {
        SmsTemplateResponse resp = adminMarketingService.createTemplate(request.getTemplateType(), request.getMessageTemplate(), request.getDescription());
        return ResponseEntity.ok(ApiResponse.success("Template created", resp));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteTemplate(@PathVariable Long id) {
        adminMarketingService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted", null));
    }

    @PostMapping("/campaigns/send")
    public ResponseEntity<ApiResponse<Object>> sendCampaign(@RequestBody MarketingCampaignRequest request) {
        int sent = adminMarketingService.sendCampaign(request);
        return ResponseEntity.ok(ApiResponse.success("Campaign sent", java.util.Map.of("sent", sent)));
    }
}
