package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.RewardItemRequest;
import com.restaurant.waitlist.backend.dto.response.admin.RewardItemResponse;
import com.restaurant.waitlist.backend.entity.RewardItem;
import com.restaurant.waitlist.backend.repository.RewardItemRepository;
import com.restaurant.waitlist.backend.service.admin.AdminRewardItemService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminRewardItemServiceImpl implements AdminRewardItemService {
    private static final Logger log = LoggerFactory.getLogger(AdminRewardItemServiceImpl.class);
    
    private final RewardItemRepository rewardItemRepository;
    
    private RewardItemResponse mapToResponse(RewardItem item) {
        if (item == null) return null;
        return RewardItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .pointsCost(item.getPointsCost())
                .icon(item.getIcon())
                .category(item.getCategory())
                .available(item.getAvailable())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    @Override
    public Page<RewardItemResponse> listRewardItems(Long restaurantId, String category, Boolean available, Pageable pageable) {
        log.info("Listing reward items - restaurantId: {}, category: {}, available: {}", restaurantId, category, available);
        Page<RewardItem> items;
        
        if (restaurantId != null && category != null && available != null) {
            items = rewardItemRepository.findByRestaurantIdAndCategoryAndAvailableTrue(restaurantId, category, pageable);
        } else if (restaurantId != null && available != null) {
            items = rewardItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId, pageable);
        } else if (restaurantId != null) {
            items = rewardItemRepository.findByRestaurantId(restaurantId, pageable);
        } else {
            items = rewardItemRepository.findAll(pageable);
        }
        
        return items.map(item -> modelMapper.map(item, RewardItemResponse.class));
    }

    @Override
    public RewardItemResponse getRewardItemById(Long itemId) {
        log.info("Getting reward item - itemId: {}", itemId);
        RewardItem item = rewardItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Reward item not found"));
        return mapToResponse(item);
    }

    @Override
    public RewardItemResponse createRewardItem(RewardItemRequest request) {
        log.info("Creating reward item - restaurantId: {}, title: {}", request.getRestaurantId(), request.getTitle());
        RewardItem item = RewardItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .pointsCost(request.getPointsCost())
                .icon(request.getIcon())
                .category(request.getCategory())
                .available(request.getAvailable())
                .build();
        item = rewardItemRepository.save(item);
        return mapToResponse(item);
    }

    @Override
    public RewardItemResponse updateRewardItem(Long itemId, RewardItemRequest request) {
        log.info("Updating reward item - itemId: {}", itemId);
        RewardItem item = rewardItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Reward item not found"));
        
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setPointsCost(request.getPointsCost());
        item.setIcon(request.getIcon());
        item.setCategory(request.getCategory());
        item.setAvailable(request.getAvailable());
        item = rewardItemRepository.save(item);
        return mapToResponse(item);
    }

    @Override
    public void deleteRewardItem(Long itemId) {
        log.info("Deleting reward item - itemId: {}", itemId);
        rewardItemRepository.deleteById(itemId);
    }

    @Override
    public RewardItemResponse toggleAvailability(Long itemId) {
        log.info("Toggling reward item availability - itemId: {}", itemId);
        RewardItem item = rewardItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Reward item not found"));
        
        item.setAvailable(!item.getAvailable());
        item = rewardItemRepository.save(item);
        return mapToResponse(item);
    }

    @Override
    public Map<String, Object> bulkToggleAvailability(List<Long> itemIds, Boolean available) {
        log.info("Bulk toggling availability for {} items", itemIds.size());
        int updated = 0;
        
        for (Long itemId : itemIds) {
            RewardItem item = rewardItemRepository.findById(itemId).orElse(null);
            if (item != null) {
                item.setAvailable(available != null ? available : !item.getAvailable());
                rewardItemRepository.save(item);
                updated++;
            }
        }
        
        return Map.of(
            "totalRequested", itemIds.size(),
            "updated", updated
        );
    }

    @Override
    public Page<RewardItemResponse> getByCategory(Long restaurantId, String category, Pageable pageable) {
        log.info("Getting items by category - category: {}", category);
        Page<RewardItem> items = rewardItemRepository.findByRestaurantIdAndCategoryAndAvailableTrue(restaurantId, category, pageable);
        return items.map(this::mapToResponse);
    }

    @Override
    public List<String> getAvailableCategories(Long restaurantId) {
        log.info("Getting available categories");
        // This would need a custom query in repository
        return List.of("beverage", "food", "dessert", "special");
    }

    @Override
    public RewardItemResponse duplicateRewardItem(Long itemId, String newTitle) {
        log.info("Duplicating reward item - itemId: {}", itemId);
        RewardItem original = rewardItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Reward item not found"));
        
        RewardItem duplicate = RewardItem.builder()
                .restaurant(original.getRestaurant())
                .title(newTitle != null ? newTitle : original.getTitle() + " (Copy)")
                .description(original.getDescription())
                .pointsCost(original.getPointsCost())
                .icon(original.getIcon())
                .category(original.getCategory())
                .available(true)
                .build();
        
        duplicate = rewardItemRepository.save(duplicate);
        return mapToResponse(duplicate);
    }
}
