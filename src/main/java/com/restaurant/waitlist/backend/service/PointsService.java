package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.PointsLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointsService {
    long getBalance(Long userId);
    com.restaurant.waitlist.backend.entity.DinerlyPoints getPointsEntity(Long userId);
    long credit(Long userId, long amount, String reason, String sourceType, Long sourceId, String admin);
    long debit(Long userId, long amount, String reason, String sourceType, Long sourceId) throws InsufficientPointsException;
    Page<PointsLedger> getLedger(Long userId, Pageable pageable);

    class InsufficientPointsException extends RuntimeException {
        public InsufficientPointsException(String message) { super(message); }
    }
}
