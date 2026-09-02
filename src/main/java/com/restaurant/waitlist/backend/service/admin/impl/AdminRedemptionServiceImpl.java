package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.RedemptionResponse;
import com.restaurant.waitlist.backend.entity.Redemption;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.service.admin.AdminRedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminRedemptionServiceImpl implements AdminRedemptionService {

    private final RedemptionRepository redemptionRepository;

    @Override
    public Page<RedemptionResponse> listRedemptions(Long locationId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<Redemption> page = redemptionRepository.findFiltered(locationId, from, to, pageable);
        return page.map(this::map);
    }

    @Override
    public void exportRedemptionsCsv(Long locationId, LocalDateTime from, LocalDateTime to, OutputStream out) throws java.io.IOException {
        // stream using pagination to avoid loading everything into memory
        int page = 0;
        int size = 500;
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.println("id,itemRedeemed,location,guest,redeemedAt,value");
            org.springframework.data.domain.Page<Redemption> p;
            do {
                p = redemptionRepository.findFiltered(locationId, from, to, org.springframework.data.domain.PageRequest.of(page, size));
                for (Redemption r : p.getContent()) {
                    String line = String.format("%d,%s,%s,%s,%s,%s",
                            r.getId(),
                            r.getOffer() != null ? r.getOffer().getName().replaceAll(",", " ") : "",
                            r.getOffer() != null && r.getOffer().getRestaurant() != null ? r.getOffer().getRestaurant().getName().replaceAll(",", " ") : "",
                            r.getGuestName() != null ? r.getGuestName().replaceAll(",", " ") : "",
                            r.getRedeemedAt() != null ? r.getRedeemedAt().toString() : "",
                            r.getValue() != null ? r.getValue().toPlainString() : "0"
                    );
                    writer.println(line);
                }
                writer.flush();
                page++;
            } while (!p.isLast());
        }
    }

    private RedemptionResponse map(Redemption r) {
        RedemptionResponse resp = new RedemptionResponse();
        resp.setId(r.getId());
        resp.setItemRedeemed(r.getOffer() != null ? r.getOffer().getName() : null);
        resp.setLocation(r.getOffer() != null && r.getOffer().getRestaurant() != null ? r.getOffer().getRestaurant().getName() : null);
        resp.setGuest(r.getGuestName());
        resp.setRedeemedAt(r.getRedeemedAt());
        resp.setValue(r.getValue());
        return resp;
    }
}
