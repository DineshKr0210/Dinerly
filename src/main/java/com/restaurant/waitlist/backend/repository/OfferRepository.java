package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.offer.id = ?1")
    long countRedemptionsByOfferId(Long offerId);

    // Franchise-group scoped variant: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    @Query("SELECT o FROM Offer o WHERE o.restaurant.id IN :restaurantIds " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:fromDate IS NULL OR o.startDate >= :fromDate) " +
            "AND (:toDate IS NULL OR o.endDate <= :toDate)")
    Page<Offer> findFilteredByRestaurantIds(List<Long> restaurantIds, String status, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    @Query("SELECT o FROM Offer o WHERE o.restaurant.id = :restaurantId " +
            "AND o.status = 'ACTIVE' AND o.endDate >= CURRENT_DATE " +
            "ORDER BY o.startDate DESC")
    Page<Offer> findActiveOffersByRestaurant(Long restaurantId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.offer.id = :offerId AND r.userId = :userId")
    long countTotalRedemptionsByUserAndOffer(Long offerId, Long userId);

    @Query("SELECT COUNT(o) FROM Offer o WHERE o.restaurant.id = :restaurantId " +
            "AND o.status = 'ACTIVE' AND o.endDate >= CURRENT_DATE")
    long countActiveOffersByRestaurant(Long restaurantId);

    // Batched IN-list variant, grouped by restaurant, to avoid one query per restaurant
    // when checking which locations in a franchise group have active offers.
    @Query("SELECT o.restaurant.id, COUNT(o) FROM Offer o WHERE o.restaurant.id IN :restaurantIds " +
            "AND o.status = 'ACTIVE' AND o.endDate >= CURRENT_DATE GROUP BY o.restaurant.id")
    List<Object[]> countActiveOffersGroupedByRestaurantIds(List<Long> restaurantIds);
}
