package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.RewardItemRequest;
import com.restaurant.waitlist.backend.dto.response.admin.RewardItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AdminRewardItemService {
    Page<RewardItemResponse> listRewardItems(Long restaurantId, String category, Boolean available, Pageable pageable);
    
    RewardItemResponse getRewardItemById(Long itemId);
    
    RewardItemResponse createRewardItem(RewardItemRequest request);
    
    RewardItemResponse updateRewardItem(Long itemId, RewardItemRequest request);
    
    void deleteRewardItem(Long itemId);
    
    RewardItemResponse toggleAvailability(Long itemId);
    
    Map<String, Object> bulkToggleAvailability(List<Long> itemIds, Boolean available);
    
    Page<RewardItemResponse> getByCategory(Long restaurantId, String category, Pageable pageable);
    
    List<String> getAvailableCategories(Long restaurantId);
    
    RewardItemResponse duplicateRewardItem(Long itemId, String newTitle);
}
