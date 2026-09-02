package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

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
}
