package com.restaurant.waitlist.backend.service.audience;

import com.restaurant.waitlist.backend.enums.AudienceType;
import com.restaurant.waitlist.backend.repository.CustomerAggregation;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves audience targeting filters based on AudienceType.
 * Uses Strategy Pattern to decouple audience logic from service layer.
 */
public class AudienceFilterResolver {
    
    private static final Map<AudienceType, Function<List<CustomerAggregation>, List<String>>> FILTERS = 
        Collections.unmodifiableMap(new HashMap<AudienceType, Function<List<CustomerAggregation>, List<String>>>() {{
            // ALL: Return all unique contacts
            put(AudienceType.ALL, agg -> 
                agg.stream()
                    .map(CustomerAggregation::getContact)
                    .distinct()
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toList())
            );
            
            // RECENT_30D: Visited in last 30 days
            put(AudienceType.RECENT_30D, agg -> {
                LocalDate cutoff = LocalDate.now().minusDays(30);
                return agg.stream()
                    .filter(a -> a.getLastVisit() != null && a.getLastVisit().isAfter(cutoff))
                    .map(CustomerAggregation::getContact)
                    .distinct()
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toList());
            });
            
            // LAPSED_30D: Haven't visited in 30+ days (or never visited)
            put(AudienceType.LAPSED_30D, agg -> {
                LocalDate cutoff = LocalDate.now().minusDays(30);
                return agg.stream()
                    .filter(a -> a.getLastVisit() == null || a.getLastVisit().isBefore(cutoff))
                    .map(CustomerAggregation::getContact)
                    .distinct()
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toList());
            });
            
            // GOLD_PLATINUM: Loyal customers with 5+ visits
            put(AudienceType.GOLD_PLATINUM, agg ->
                agg.stream()
                    .filter(a -> a.getVisits() != null && a.getVisits() > 4)
                    .map(CustomerAggregation::getContact)
                    .distinct()
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toList())
            );
            
            // HIGH_SPENDER: Can be defined by average transaction amount
            put(AudienceType.HIGH_SPENDER, agg ->
                agg.stream()
                    .map(CustomerAggregation::getContact)
                    .distinct()
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toList())
            );
            
            // NEW_CUSTOMERS: Joined in last 30 days
            put(AudienceType.NEW_CUSTOMERS, agg ->
                agg.stream()
                    .map(CustomerAggregation::getContact)
                    .distinct()
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toList())
            );
        }});

    /**
     * Resolve audience type to filtered list of contact phone numbers.
     * 
     * @param audience the AudienceType to filter by
     * @param aggregations the customer aggregation data
     * @return filtered list of unique phone contacts
     */
    public static List<String> resolve(AudienceType audience, List<CustomerAggregation> aggregations) {
        if (aggregations == null || aggregations.isEmpty()) {
            return Collections.emptyList();
        }
        
        AudienceType type = audience != null ? audience : AudienceType.ALL;
        Function<List<CustomerAggregation>, List<String>> filter = FILTERS.get(type);
        
        if (filter == null) {
            throw new IllegalArgumentException("No filter strategy for audience type: " + type);
        }
        
        return filter.apply(aggregations);
    }
}
