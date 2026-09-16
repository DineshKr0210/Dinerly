package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.RewardItemRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RewardItemResponse;
import com.restaurant.waitlist.backend.service.admin.AdminRewardItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/reward-items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRewardItemController {

    private final AdminRewardItemService adminRewardItemService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RewardItemResponse>>> list(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RewardItemResponse> resp = adminRewardItemService.listRewardItems(restaurantId, category, available, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reward items retrieved successfully", resp));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<RewardItemResponse>> getById(@PathVariable Long itemId) {
        RewardItemResponse resp = adminRewardItemService.getRewardItemById(itemId);
        return ResponseEntity.ok(ApiResponse.success("Reward item retrieved successfully", resp));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RewardItemResponse>> create(@Valid @RequestBody RewardItemRequest request) {
        RewardItemResponse resp = adminRewardItemService.createRewardItem(request);
        return ResponseEntity.ok(ApiResponse.success("Reward item created successfully", resp));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<RewardItemResponse>> update(
            @PathVariable Long itemId,
            @Valid @RequestBody RewardItemRequest request) {
        RewardItemResponse resp = adminRewardItemService.updateRewardItem(itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Reward item updated successfully", resp));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long itemId) {
        adminRewardItemService.deleteRewardItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Reward item deleted successfully"));
    }

    @PutMapping("/{itemId}/toggle-availability")
    public ResponseEntity<ApiResponse<RewardItemResponse>> toggleAvailability(@PathVariable Long itemId) {
        RewardItemResponse resp = adminRewardItemService.toggleAvailability(itemId);
        return ResponseEntity.ok(ApiResponse.success("Reward item availability toggled successfully", resp));
    }

    @PostMapping("/bulk-toggle-availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkToggleAvailability(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        java.util.List<Long> itemIds = (java.util.List<Long>) request.get("itemIds");
        Boolean available = (Boolean) request.get("available");
        Map<String, Object> resp = adminRewardItemService.bulkToggleAvailability(itemIds, available);
        return ResponseEntity.ok(ApiResponse.success("Items availability updated successfully", resp));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<RewardItemResponse>>> getByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RewardItemResponse> resp = adminRewardItemService.getByCategory(restaurantId, category, pageable);
        return ResponseEntity.ok(ApiResponse.success("Items retrieved by category successfully", resp));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<java.util.List<String>>> getCategories(
            @RequestParam(required = false) Long restaurantId) {
        java.util.List<String> resp = adminRewardItemService.getAvailableCategories(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", resp));
    }

    @PostMapping("/{itemId}/duplicate")
    public ResponseEntity<ApiResponse<RewardItemResponse>> duplicate(
            @PathVariable Long itemId,
            @RequestParam(required = false) String newTitle) {
        RewardItemResponse resp = adminRewardItemService.duplicateRewardItem(itemId, newTitle);
        return ResponseEntity.ok(ApiResponse.success("Reward item duplicated successfully", resp));
    }
}
