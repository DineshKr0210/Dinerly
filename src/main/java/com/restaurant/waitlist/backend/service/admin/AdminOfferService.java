package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.OfferRequest;
import com.restaurant.waitlist.backend.dto.response.admin.OfferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminOfferService {
    Page<OfferResponse> listOffers(Long locationId, String status, String category, Pageable pageable);
    OfferResponse getOfferById(Long offerId);
    OfferResponse createOffer(OfferRequest request);
    OfferResponse updateOffer(Long offerId, OfferRequest request);
    void deleteOffer(Long offerId);
    OfferResponse toggleOfferStatus(Long offerId);
    OfferResponse duplicateOffer(Long offerId, String newName);
    Map<String, Object> bulkDuplicateOffers(List<Long> offerIds);
    OfferResponse archiveOffer(Long offerId);
    Map<String, Object> bulkArchiveOffers(List<Long> offerIds);
    List<String> getAvailableCategories(Long locationId);
    Page<OfferResponse> getOffersByCategory(String category, Long locationId, Pageable pageable);
    Page<OfferResponse> getOfferExpiringSoon(Long locationId, int days, Pageable pageable);
    Page<OfferResponse> getOffersWithLowInventory(Long locationId, int threshold, Pageable pageable);
    void exportOffersCsv(Long locationId, String status, LocalDateTime from, LocalDateTime to, OutputStream out) throws Exception;
    Map<String, Object> getStatistics(Long locationId);
    Map<String, Object> bulkUpdateInventory(List<Map<String, Object>> updates);
}
