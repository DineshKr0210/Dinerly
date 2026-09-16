package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Redemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RedemptionRepository extends JpaRepository<Redemption, Long>, RedemptionRepositoryCustom {
    Page<Redemption> findByRestaurantId(Long restaurantId, Pageable pageable);

    // Phase 2: Code-based lookups
    Optional<Redemption> findByRedemptionCode(String code);

    boolean existsByRedemptionCode(String code);

    @Query("SELECT r FROM Redemption r WHERE r.redemptionCode = :code " +
            "AND r.status = 'GENERATED' AND r.codeExpiresAt > CURRENT_TIMESTAMP")
    Optional<Redemption> findValidRedemptionCode(String code);

    @Query("SELECT r FROM Redemption r WHERE r.userId = :userId AND r.offer.id = :offerId " +
            "AND FUNCTION('DATE', r.redeemedAt) = CURRENT_DATE")
    List<Redemption> findTodayRedemptionsByUserAndOffer(Long userId, Long offerId);

    @Query("SELECT r FROM Redemption r WHERE r.userId = :userId AND r.offer.id = :offerId")
    List<Redemption> findAllRedemptionsByUserAndOffer(Long userId, Long offerId);

    @Query("SELECT r FROM Redemption r WHERE r.status = 'EXPIRED' OR r.codeExpiresAt < CURRENT_TIMESTAMP")
    List<Redemption> findExpiredCodes();
}
