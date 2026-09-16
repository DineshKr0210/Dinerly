package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.PointsEarningRuleRequest;
import com.restaurant.waitlist.backend.dto.response.admin.PointsEarningRuleResponse;
import com.restaurant.waitlist.backend.entity.PointsEarningRule;
import com.restaurant.waitlist.backend.repository.PointsEarningRuleRepository;
import com.restaurant.waitlist.backend.service.admin.AdminPointsEarningRuleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
public class AdminPointsEarningRuleServiceImpl implements AdminPointsEarningRuleService {
    private static final Logger log = LoggerFactory.getLogger(AdminPointsEarningRuleServiceImpl.class);
    
    private final PointsEarningRuleRepository pointsEarningRuleRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<PointsEarningRuleResponse> listEarningRules(Long restaurantId, String action, Pageable pageable) {
        log.info("Listing earning rules - restaurantId: {}, action: {}", restaurantId, action);
        Page<PointsEarningRule> rules;
        
        if (restaurantId != null && action != null) {
            rules = pointsEarningRuleRepository.findByRestaurantIdAndAction(restaurantId, action, pageable);
        } else if (restaurantId != null) {
            rules = pointsEarningRuleRepository.findByRestaurantId(restaurantId, pageable);
        } else {
            rules = pointsEarningRuleRepository.findAll(pageable);
        }
        
        return rules.map(rule -> modelMapper.map(rule, PointsEarningRuleResponse.class));
    }

    @Override
    public PointsEarningRuleResponse getEarningRuleById(Long ruleId) {
        log.info("Getting earning rule - ruleId: {}", ruleId);
        PointsEarningRule rule = pointsEarningRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Earning rule not found"));
        return modelMapper.map(rule, PointsEarningRuleResponse.class);
    }

    @Override
    public PointsEarningRuleResponse createEarningRule(PointsEarningRuleRequest request) {
        log.info("Creating earning rule - restaurantId: {}, action: {}", request.getRestaurantId(), request.getAction());
        PointsEarningRule rule = modelMapper.map(request, PointsEarningRule.class);
        rule = pointsEarningRuleRepository.save(rule);
        return modelMapper.map(rule, PointsEarningRuleResponse.class);
    }

    @Override
    public PointsEarningRuleResponse updateEarningRule(Long ruleId, PointsEarningRuleRequest request) {
        log.info("Updating earning rule - ruleId: {}", ruleId);
        PointsEarningRule rule = pointsEarningRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Earning rule not found"));
        
        modelMapper.map(request, rule);
        rule = pointsEarningRuleRepository.save(rule);
        return modelMapper.map(rule, PointsEarningRuleResponse.class);
    }

    @Override
    public void deleteEarningRule(Long ruleId) {
        log.info("Deleting earning rule - ruleId: {}", ruleId);
        pointsEarningRuleRepository.deleteById(ruleId);
    }

    @Override
    public List<PointsEarningRuleResponse> getByAction(Long restaurantId, String action) {
        log.info("Getting rules by action - action: {}", action);
        List<PointsEarningRule> rules = pointsEarningRuleRepository.findByRestaurantIdAndAction(restaurantId, action);
        return rules.stream()
                .map(rule -> modelMapper.map(rule, PointsEarningRuleResponse.class))
                .collect(Collectors.toList());
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
        
        rule.setActive(!rule.getActive());
        rule = pointsEarningRuleRepository.save(rule);
        return modelMapper.map(rule, PointsEarningRuleResponse.class);
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
}
