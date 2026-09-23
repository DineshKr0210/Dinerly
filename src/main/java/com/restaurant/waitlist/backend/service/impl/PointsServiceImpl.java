package com.restaurant.waitlist.backend.service.impl;

import com.restaurant.waitlist.backend.entity.DinerlyPoints;
import com.restaurant.waitlist.backend.entity.PointsLedger;
import com.restaurant.waitlist.backend.repository.DinerlyPointsRepository;
import com.restaurant.waitlist.backend.repository.PointsLedgerRepository;
import com.restaurant.waitlist.backend.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {
    private static final Logger log = LoggerFactory.getLogger(PointsServiceImpl.class);

    private final DinerlyPointsRepository pointsRepository;
    private final PointsLedgerRepository ledgerRepository;

    @Override
    @Transactional(readOnly = true)
    public long getBalance(Long userId) {
        return pointsRepository.findByUserId(userId).map(DinerlyPoints::getBalance).orElse(0L);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public long credit(Long userId, long amount, String reason, String sourceType, Long sourceId, String admin) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                DinerlyPoints dp = pointsRepository.findByUserId(userId).orElseGet(() -> DinerlyPoints.builder().userId(userId).balance(0L).build());
                long newBalance = dp.getBalance() + amount;
                dp.setBalance(newBalance);
                dp = pointsRepository.save(dp);
                PointsLedger ledger = PointsLedger.builder()
                        .userId(userId)
                        .delta(amount)
                        .balanceAfter(newBalance)
                        .reason(reason)
                        .sourceType(sourceType)
                        .sourceId(sourceId)
                        .createdBy(admin)
                        .build();
                ledgerRepository.save(ledger);
                return newBalance;
            } catch (OptimisticLockingFailureException e) {
                log.warn("Optimistic lock, retrying credit for user={}", userId);
            }
        }
        throw new RuntimeException("Failed to credit points after retries");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public long debit(Long userId, long amount, String reason, String sourceType, Long sourceId) throws PointsService.InsufficientPointsException {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                DinerlyPoints dp = pointsRepository.findByUserId(userId).orElseGet(() -> DinerlyPoints.builder().userId(userId).balance(0L).build());
                long balance = dp.getBalance();
                if (balance < amount) throw new PointsService.InsufficientPointsException("Insufficient points");
                long newBalance = balance - amount;
                dp.setBalance(newBalance);
                dp = pointsRepository.save(dp);
                PointsLedger ledger = PointsLedger.builder()
                        .userId(userId)
                        .delta(-amount)
                        .balanceAfter(newBalance)
                        .reason(reason)
                        .sourceType(sourceType)
                        .sourceId(sourceId)
                        .createdBy("system")
                        .build();
                ledgerRepository.save(ledger);
                return newBalance;
            } catch (OptimisticLockingFailureException e) {
                log.warn("Optimistic lock, retrying debit for user={}", userId);
            }
        }
        throw new RuntimeException("Failed to debit points after retries");
    }

    @Override
    @Transactional(readOnly = true)
    public DinerlyPoints getPointsEntity(Long userId) {
        return pointsRepository.findByUserId(userId)
            .orElseGet(() -> DinerlyPoints.builder()
                .userId(userId)
                .balance(0L)
                .build());
    }
}
