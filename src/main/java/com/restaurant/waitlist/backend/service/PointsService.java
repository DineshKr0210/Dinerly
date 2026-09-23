package com.restaurant.waitlist.backend.service;

public interface PointsService {
    long getBalance(Long userId);
    com.restaurant.waitlist.backend.entity.DinerlyPoints getPointsEntity(Long userId);
    long credit(Long userId, long amount, String reason, String sourceType, Long sourceId, String admin);
    long debit(Long userId, long amount, String reason, String sourceType, Long sourceId) throws InsufficientPointsException;

    class InsufficientPointsException extends RuntimeException {
        public InsufficientPointsException(String message) { super(message); }
    }
}
