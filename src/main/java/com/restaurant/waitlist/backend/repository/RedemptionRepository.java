package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Redemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RedemptionRepository extends JpaRepository<Redemption, Long>, RedemptionRepositoryCustom {
    boolean existsByRedemptionCode(String code);

    @Query("SELECT r FROM Redemption r WHERE r.redemptionCode = :code " +
            "AND r.status = 'GENERATED' AND r.codeExpiresAt > CURRENT_TIMESTAMP")
    Optional<Redemption> findValidRedemptionCode(String code);

    // Campaign redemption tracking queries
    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.campaign.id = :campaignId")
    long countByCampaignId(Long campaignId);

    // Double-redemption guard for shared campaign coupon codes: has this guest
    // already redeemed this campaign's code?
    boolean existsByCampaignIdAndGuestPhoneAndStatus(Long campaignId, String guestPhone, Redemption.RedemptionStatus status);

    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.restaurantId = :restaurantId AND r.redeemedAt >= :fromDate AND r.redeemedAt <= :toDate")
    long countRedemptionsByRestaurantAndDateRange(Long restaurantId, java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate);
}
