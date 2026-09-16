package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.PointsEarningRuleRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.PointsEarningRuleResponse;
import com.restaurant.waitlist.backend.service.admin.AdminPointsEarningRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/points/earning-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPointsEarningRuleController {

    private final AdminPointsEarningRuleService adminPointsEarningRuleService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PointsEarningRuleResponse>>> list(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PointsEarningRuleResponse> resp = adminPointsEarningRuleService.listEarningRules(restaurantId, action, pageable);
        return ResponseEntity.ok(ApiResponse.success("Earning rules retrieved successfully", resp));
    }

    @GetMapping("/{ruleId}")
    public ResponseEntity<ApiResponse<PointsEarningRuleResponse>> getById(@PathVariable Long ruleId) {
        PointsEarningRuleResponse resp = adminPointsEarningRuleService.getEarningRuleById(ruleId);
        return ResponseEntity.ok(ApiResponse.success("Earning rule retrieved successfully", resp));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PointsEarningRuleResponse>> create(@Valid @RequestBody PointsEarningRuleRequest request) {
        PointsEarningRuleResponse resp = adminPointsEarningRuleService.createEarningRule(request);
        return ResponseEntity.ok(ApiResponse.success("Earning rule created successfully", resp));
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<ApiResponse<PointsEarningRuleResponse>> update(
            @PathVariable Long ruleId,
            @Valid @RequestBody PointsEarningRuleRequest request) {
        PointsEarningRuleResponse resp = adminPointsEarningRuleService.updateEarningRule(ruleId, request);
        return ResponseEntity.ok(ApiResponse.success("Earning rule updated successfully", resp));
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long ruleId) {
        adminPointsEarningRuleService.deleteEarningRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success("Earning rule deleted successfully"));
    }

    @GetMapping("/by-action/{action}")
    public ResponseEntity<ApiResponse<List<PointsEarningRuleResponse>>> getByAction(
            @PathVariable String action,
            @RequestParam(required = false) Long restaurantId) {
        List<PointsEarningRuleResponse> resp = adminPointsEarningRuleService.getByAction(restaurantId, action);
        return ResponseEntity.ok(ApiResponse.success("Rules retrieved by action successfully", resp));
    }

    @GetMapping("/actions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableActions(
            @RequestParam(required = false) Long restaurantId) {
        List<Map<String, Object>> resp = adminPointsEarningRuleService.getAvailableActions(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Available actions retrieved successfully", resp));
    }

    @PutMapping("/{ruleId}/toggle-active")
    public ResponseEntity<ApiResponse<PointsEarningRuleResponse>> toggleActive(@PathVariable Long ruleId) {
        PointsEarningRuleResponse resp = adminPointsEarningRuleService.toggleActive(ruleId);
        return ResponseEntity.ok(ApiResponse.success("Rule status toggled successfully", resp));
    }

    @PostMapping("/bulk-update-points")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkUpdatePoints(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> updates = (List<Map<String, Object>>) request.get("updates");
        Map<String, Object> resp = adminPointsEarningRuleService.bulkUpdatePoints(updates);
        return ResponseEntity.ok(ApiResponse.success("Points values updated successfully", resp));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @RequestParam(required = false) Long restaurantId) {
        Map<String, Object> resp = adminPointsEarningRuleService.getStatistics(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", resp));
    }
}
