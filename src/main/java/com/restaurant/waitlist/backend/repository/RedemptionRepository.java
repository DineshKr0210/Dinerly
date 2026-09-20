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

    // Campaign redemption tracking queries
    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.campaign.id = :campaignId")
    long countByCampaignId(Long campaignId);

    @Query("SELECT r FROM Redemption r WHERE r.campaign.id = :campaignId ORDER BY r.redeemedAt DESC")
    Page<Redemption> findByCampaignId(Long campaignId, Pageable pageable);

    // Campaign code validation - find valid campaign-specific code
    @Query("SELECT r FROM Redemption r WHERE r.redemptionCode = :code AND r.campaign.id = :campaignId " +
            "AND r.status = 'GENERATED' AND r.codeExpiresAt > CURRENT_TIMESTAMP")
    Optional<Redemption> findValidCampaignCode(String code, Long campaignId);

    // Campaign code validation - find valid campaign-specific code by code only (campaign auto-looked-up)
    @Query("SELECT r FROM Redemption r WHERE r.redemptionCode = :code AND r.campaign IS NOT NULL " +
            "AND r.status = 'GENERATED' AND r.codeExpiresAt > CURRENT_TIMESTAMP")
    Optional<Redemption> findValidCampaignCodeByCodeOnly(String code);

    // Count codes generated for a campaign (status = GENERATED)
    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.campaign.id = :campaignId AND r.status = 'GENERATED'")
    int countCodesGeneratedForCampaign(Long campaignId);

    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.restaurantId = :restaurantId AND r.redeemedAt >= :fromDate AND r.redeemedAt <= :toDate")
    long countRedemptionsByRestaurantAndDateRange(Long restaurantId, java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate);

    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.redeemedAt >= :fromDate AND r.redeemedAt <= :toDate")
    long countRedemptionsByDateRange(java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate);
}
