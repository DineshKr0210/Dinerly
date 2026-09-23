package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.AuditLog;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Small wrapper around the repeated "build an AuditLog and save it" block
 * that used to be duplicated in every admin service.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long restaurantId, String action, String details) {
        auditLogRepository.save(AuditLog.builder()
                .restaurantId(restaurantId)
                .action(action)
                .details(details)
                .build());
    }
}
