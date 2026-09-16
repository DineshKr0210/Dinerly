package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Page<Offer> findByRestaurantId(Long restaurantId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.offer.id = ?1")
    long countRedemptionsByOfferId(Long offerId);

    @Query("SELECT o FROM Offer o WHERE (:restaurantId IS NULL OR o.restaurant.id = :restaurantId) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:fromDate IS NULL OR o.startDate >= :fromDate) " +
            "AND (:toDate IS NULL OR o.endDate <= :toDate)")
    Page<Offer> findFiltered(Long restaurantId, String status, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    // Phase 2: Category filtering and status filtering
    @Query("SELECT o FROM Offer o WHERE o.restaurant.id = :restaurantId " +
            "AND o.category = :category AND o.status = 'ACTIVE' " +
            "AND o.endDate >= CURRENT_DATE ORDER BY o.rating DESC NULLS LAST")
    Page<Offer> findByRestaurantIdAndCategoryAndActive(Long restaurantId, String category, Pageable pageable);

    @Query("SELECT o FROM Offer o WHERE o.restaurant.id = :restaurantId " +
            "AND o.status = 'ACTIVE' AND o.endDate >= CURRENT_DATE " +
            "ORDER BY o.rating DESC NULLS LAST")
    Page<Offer> findActiveOffersByRestaurant(Long restaurantId, Pageable pageable);

    @Query("SELECT o FROM Offer o WHERE o.status = 'ACTIVE' AND o.endDate >= CURRENT_DATE " +
            "ORDER BY o.rating DESC NULLS LAST")
    Page<Offer> findAllActive(Pageable pageable);

    // Phase 2: Redemption code lookup (will use Redemption entity)
    @Query("SELECT r FROM Redemption r WHERE r.redemptionCode = :code " +
            "AND r.status = 'GENERATED' AND r.codeExpiresAt > CURRENT_TIMESTAMP")
    Optional<Object> findByRedemptionCode(String code);

    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.offer.id = :offerId " +
            "AND r.userId = :userId AND FUNCTION('DATE', r.redeemedAt) = CURRENT_DATE")
    long countTodayRedemptionsByUserAndOffer(Long offerId, Long userId);

    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.offer.id = :offerId AND r.userId = :userId")
    long countTotalRedemptionsByUserAndOffer(Long offerId, Long userId);
}
