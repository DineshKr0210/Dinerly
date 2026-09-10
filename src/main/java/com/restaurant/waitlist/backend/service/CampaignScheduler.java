package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.Campaign;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.service.admin.AdminCampaignService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CampaignScheduler {
    private static final Logger log = LoggerFactory.getLogger(CampaignScheduler.class);

    private final CampaignRepository campaignRepository;
    private final AdminCampaignService adminCampaignService;

    // Poll every minute by default; configurable via property campaign.sender.poll-ms
    @Scheduled(fixedDelayString = "${campaign.sender.poll-ms:60000}")
    public void pollAndSend() {
        try {
            LocalDateTime now = LocalDateTime.now();
            var page = campaignRepository.findByStatusAndScheduledAtBefore("SCHEDULED", now, PageRequest.of(0, 50));
            if (page == null || page.isEmpty()) return;
            for (Campaign c : page.getContent()) {
                try {
                    log.info("Scheduler: processing campaign id={} scheduledAt={}", c.getId(), c.getScheduledAt());
                    // force immediate send for any scheduled campaign whose time has come
                    adminCampaignService.publishCampaign(c.getId(), true);
                } catch (Exception e) {
                    log.error("Error sending campaign id={}: {}", c.getId(), e.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error("CampaignScheduler poll failed: {}", ex.getMessage(), ex);
        }
    }
}
