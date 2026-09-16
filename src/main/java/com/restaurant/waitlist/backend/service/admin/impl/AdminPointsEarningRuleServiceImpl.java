package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.PointsEarningRuleRequest;
import com.restaurant.waitlist.backend.dto.response.admin.PointsEarningRuleResponse;
import com.restaurant.waitlist.backend.entity.PointsEarningRule;
import com.restaurant.waitlist.backend.repository.PointsEarningRuleRepository;
import com.restaurant.waitlist.backend.service.admin.AdminPointsEarningRuleService;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPointsEarningRuleServiceImpl implements AdminPointsEarningRuleService {
    private static final Logger log = LoggerFactory.getLogger(AdminPointsEarningRuleServiceImpl.class);
    
    private final PointsEarningRuleRepository pointsEarningRuleRepository;

    @Override
    public Page<PointsEarningRuleResponse> listEarningRules(Long restaurantId, String action, Pageable pageable) {
        log.info("Listing earning rules - restaurantId: {}, action: {}", restaurantId, action);
        Page<PointsEarningRule> rules = pointsEarningRuleRepository.findAll(pageable);
        return rules.map(this::map);
    }

    @Override
    public PointsEarningRuleResponse getEarningRuleById(Long ruleId) {
        log.info("Getting earning rule - ruleId: {}", ruleId);
        PointsEarningRule rule = pointsEarningRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Earning rule not found"));
        return map(rule);
    }

    @Override
    public PointsEarningRuleResponse createEarningRule(PointsEarningRuleRequest request) {
        log.info("Creating earning rule - action: {}", request.getAction());
        PointsEarningRule rule = PointsEarningRule.builder()
                .action(request.getAction())
                .pointsValue(request.getPointsValue())
                .description(request.getDescription())
                .icon(request.getIcon())
                .clickable(request.getClickable() != null && request.getClickable())
                .actionUrl(request.getActionUrl())
                .build();
        rule = pointsEarningRuleRepository.save(rule);
        return map(rule);
    }

    @Override
    public PointsEarningRuleResponse updateEarningRule(Long ruleId, PointsEarningRuleRequest request) {
        log.info("Updating earning rule - ruleId: {}", ruleId);
        PointsEarningRule rule = pointsEarningRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Earning rule not found"));
        
        rule.setAction(request.getAction());
        rule.setPointsValue(request.getPointsValue());
        rule.setDescription(request.getDescription());
        rule.setIcon(request.getIcon());
        rule.setClickable(request.getClickable() != null && request.getClickable());
        rule.setActionUrl(request.getActionUrl());
        rule = pointsEarningRuleRepository.save(rule);
        return map(rule);
    }

    @Override
    public void deleteEarningRule(Long ruleId) {
        log.info("Deleting earning rule - ruleId: {}", ruleId);
        pointsEarningRuleRepository.deleteById(ruleId);
    }

    @Override
    public List<PointsEarningRuleResponse> getByAction(Long restaurantId, String action) {
        log.info("Getting rules by action - action: {}", action);
        Optional<PointsEarningRule> rule = pointsEarningRuleRepository.findByRestaurantIdAndAction(restaurantId, action);
        return rule.map(r -> List.of(map(r))).orElse(List.of());
    }

    @Override
    public List<Map<String, Object>> getAvailableActions(Long restaurantId) {
        log.info("Getting available actions");
        return List.of(
            Map.of("action", "dine_in", "display", "Dined in restaurant", "default", 15L),
            Map.of("action", "join_waitlist", "display", "Joined waitlist", "default", 10L),
            Map.of("action", "leave_review", "display", "Left a review", "default", 20L),
            Map.of("action", "refer_friend", "display", "Referred a friend", "default", 50L),
            Map.of("action", "visit_milestone", "display", "10th visit", "default", 100L)
        );
    }

    @Override
    public PointsEarningRuleResponse toggleActive(Long ruleId) {
        log.info("Toggling earning rule active status - ruleId: {}", ruleId);
        PointsEarningRule rule = pointsEarningRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Earning rule not found"));
        
        rule.setClickable(!Boolean.TRUE.equals(rule.getClickable()));
        rule = pointsEarningRuleRepository.save(rule);
        return map(rule);
    }

    @Override
    public Map<String, Object> bulkUpdatePoints(List<Map<String, Object>> updates) {
        log.info("Bulk updating points for {} rules", updates.size());
        int updated = 0;
        
        for (Map<String, Object> update : updates) {
            Long ruleId = Long.valueOf(update.get("ruleId").toString());
            Long newPoints = Long.valueOf(update.get("newPoints").toString());
            
            PointsEarningRule rule = pointsEarningRuleRepository.findById(ruleId).orElse(null);
            if (rule != null) {
                rule.setPointsValue(newPoints);
                pointsEarningRuleRepository.save(rule);
                updated++;
            }
        }
        
        return Map.of(
            "totalRequested", updates.size(),
            "updated", updated
        );
    }

    @Override
    public Map<String, Object> getStatistics(Long restaurantId) {
        log.info("Getting points earning statistics");
        return Map.of(
            "totalRules", 0,
            "activeRules", 0,
            "totalPointsDistributed", 0L,
            "averagePointsPerRule", 0L
        );
    }

    private PointsEarningRuleResponse map(PointsEarningRule r) {
        return PointsEarningRuleResponse.builder()
                .id(r.getId())
                .restaurantId(r.getRestaurant() != null ? r.getRestaurant().getId() : null)
                .action(r.getAction())
                .pointsValue(r.getPointsValue())
                .description(r.getDescription())
                .icon(r.getIcon())
                .clickable(r.getClickable())
                .actionUrl(r.getActionUrl())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
