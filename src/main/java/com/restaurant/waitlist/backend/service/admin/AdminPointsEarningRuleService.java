package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.PointsEarningRuleRequest;
import com.restaurant.waitlist.backend.dto.request.admin.UpdatePointsEarningRuleRequest;
import com.restaurant.waitlist.backend.dto.response.admin.PointsEarningRuleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AdminPointsEarningRuleService {
    Page<PointsEarningRuleResponse> listEarningRules(Long restaurantId, String action, Pageable pageable);
    
    PointsEarningRuleResponse getEarningRuleById(Long ruleId);
    
    PointsEarningRuleResponse createEarningRule(PointsEarningRuleRequest request);
    
    PointsEarningRuleResponse updateEarningRule(Long ruleId, UpdatePointsEarningRuleRequest request);
    
    void deleteEarningRule(Long ruleId);
    
    List<PointsEarningRuleResponse> getByAction(Long restaurantId, String action);
    
    List<Map<String, Object>> getAvailableActions(Long restaurantId);
    
    PointsEarningRuleResponse toggleActive(Long ruleId);
    
    Map<String, Object> bulkUpdatePoints(List<Map<String, Object>> updates);
    
    Map<String, Object> getStatistics(Long restaurantId);
}
