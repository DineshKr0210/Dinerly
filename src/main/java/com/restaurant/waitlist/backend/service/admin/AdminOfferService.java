package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.OfferRequest;
import com.restaurant.waitlist.backend.dto.response.admin.OfferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminOfferService {
    Page<OfferResponse> listOffers(Long locationId, String status, Pageable pageable);
    OfferResponse createOffer(OfferRequest request);
    OfferResponse updateOffer(Long offerId, OfferRequest request);
    void deleteOffer(Long offerId);
}
