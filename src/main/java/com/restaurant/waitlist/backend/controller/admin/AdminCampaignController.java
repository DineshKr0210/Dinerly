package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.CampaignRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.CampaignResponse;
import com.restaurant.waitlist.backend.service.admin.AdminCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/marketing/campaigns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCampaignController {

    private final AdminCampaignService adminCampaignService;

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignResponse>> create(@RequestBody CampaignRequest req) {
        CampaignResponse resp = adminCampaignService.createCampaign(req);
        return ResponseEntity.ok(ApiResponse.success("Campaign created", resp));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> update(@PathVariable Long id, @RequestBody CampaignRequest req) {
        CampaignResponse resp = adminCampaignService.updateCampaign(id, req);
        return ResponseEntity.ok(ApiResponse.success("Campaign updated", resp));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> list(@RequestParam(required = false) Long locationId,
                                                                     @RequestParam(required = false) String status,
                                                                     @RequestParam(required = false) String channel,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        Page<CampaignResponse> p = adminCampaignService.listCampaigns(PageRequest.of(page, size), locationId, status, channel);
        return ResponseEntity.ok(ApiResponse.success("Campaigns retrieved", p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> get(@PathVariable Long id) {
        CampaignResponse resp = adminCampaignService.getCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign retrieved", resp));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<CampaignResponse>> publish(@PathVariable Long id,
                                                                 @RequestParam(defaultValue = "true") boolean immediate) throws Exception {
        CampaignResponse resp = adminCampaignService.publishCampaign(id, immediate);
        return ResponseEntity.ok(ApiResponse.success("Campaign published", resp));
    }
}
