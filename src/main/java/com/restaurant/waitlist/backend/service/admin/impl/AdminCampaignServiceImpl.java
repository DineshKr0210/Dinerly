package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.CampaignRequest;
import com.restaurant.waitlist.backend.dto.response.admin.CampaignResponse;
import com.restaurant.waitlist.backend.dto.response.admin.MarketingSummaryResponse;
import com.restaurant.waitlist.backend.entity.Campaign;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.SmsService;
import com.restaurant.waitlist.backend.service.admin.AdminCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCampaignServiceImpl implements AdminCampaignService {

    private final CampaignRepository campaignRepository;
    private final WaitlistRepository waitlistRepository;
    private final SmsService smsService;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public CampaignResponse createCampaign(CampaignRequest req) {
        Campaign c = Campaign.builder()
                .name(req.getName())
                .channel(req.getChannel())
                .audience(req.getAudience())
                .templateId(req.getTemplateId())
                .message(req.getMessage())
                .restaurantId(req.getRestaurantId())
                .scheduledAt(req.getScheduledAt())
                .status(req.getScheduledAt() != null ? "SCHEDULED" : "DRAFT")
                .sentCount(0)
                .reach(0)
                .redemptions(0)
                .revenueInfluenced(null)
                .build();
        Campaign saved = campaignRepository.save(c);
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(req.getRestaurantId() != null ? req.getRestaurantId() : 0L)
                .action("CREATE_CAMPAIGN")
                .details("Campaign created: " + saved.getName())
                .build());
        return toDto(saved);
    }

    @Override
    @Transactional
    public CampaignResponse updateCampaign(Long id, CampaignRequest req) {
        Campaign c = campaignRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        c.setName(req.getName());
        c.setChannel(req.getChannel());
        c.setAudience(req.getAudience());
        c.setTemplateId(req.getTemplateId());
        c.setMessage(req.getMessage());
        c.setRestaurantId(req.getRestaurantId());
        c.setScheduledAt(req.getScheduledAt());
        if (req.getScheduledAt() != null) c.setStatus("SCHEDULED");
        Campaign saved = campaignRepository.save(c);
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(c.getRestaurantId() != null ? c.getRestaurantId() : 0L)
                .action("UPDATE_CAMPAIGN")
                .details("Campaign updated: " + saved.getName())
                .build());
        return toDto(saved);
    }

    @Override
    public Page<CampaignResponse> listCampaigns(Pageable pageable) {
        return listCampaigns(pageable, null, null, null);
    }

    @Override
    public Page<CampaignResponse> listCampaigns(Pageable pageable, Long locationId, String status, String channel) {
        Page<Campaign> p;
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
        String normalizedChannel = channel == null || channel.isBlank() ? null : channel.trim().toUpperCase(Locale.ROOT);

        if (locationId != null && normalizedStatus != null && normalizedChannel != null) {
            p = campaignRepository.findByRestaurantIdAndStatusAndChannel(locationId, normalizedStatus, normalizedChannel, pageable);
        } else if (locationId != null && normalizedStatus != null) {
            p = campaignRepository.findByRestaurantIdAndStatus(locationId, normalizedStatus, pageable);
        } else if (locationId != null && normalizedChannel != null) {
            p = campaignRepository.findByRestaurantIdAndChannel(locationId, normalizedChannel, pageable);
        } else if (locationId != null) {
            p = campaignRepository.findByRestaurantId(locationId, pageable);
        } else if (normalizedStatus != null && normalizedChannel != null) {
            p = campaignRepository.findByStatusAndChannel(normalizedStatus, normalizedChannel, pageable);
        } else if (normalizedStatus != null) {
            p = campaignRepository.findByStatus(normalizedStatus, pageable);
        } else if (normalizedChannel != null) {
            p = campaignRepository.findByChannel(normalizedChannel, pageable);
        } else {
            p = campaignRepository.findAll(pageable);
        }

        List<CampaignResponse> items = p.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(items, pageable, p.getTotalElements());
    }

    @Override
    public MarketingSummaryResponse getMarketingSummary(Long locationId) {
        List<Campaign> campaigns = (locationId != null)
                ? campaignRepository.findByRestaurantId(locationId)
                : campaignRepository.findAllByOrderByCreatedAtDesc();

        long activeCampaigns = campaigns.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();

        long guestsReached = campaigns.stream()
                .filter(c -> c.getReach() != null)
                .mapToLong(Campaign::getReach)
                .sum();

        long redemptions = campaigns.stream()
                .filter(c -> c.getRedemptions() != null)
                .mapToLong(Campaign::getRedemptions)
                .sum();

        long spendThisMonth = campaigns.stream()
                .filter(c -> c.getRevenueInfluenced() != null)
                .map(c -> c.getRevenueInfluenced())
                .filter(r -> r != null)
                .mapToLong(BigDecimal::longValue)
                .sum();

        return MarketingSummaryResponse.builder()
                .activeCampaigns(activeCampaigns)
                .guestsReached(guestsReached)
                .redemptions(redemptions)
                .spendThisMonth(spendThisMonth)
                .locationId(locationId)
                .build();
    }

    @Override
    public CampaignResponse getCampaign(Long id) {
        Campaign c = campaignRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        return toDto(c);
    }

    @Override
    @Transactional
    public CampaignResponse publishCampaign(Long id, boolean immediate) throws Exception {
        Campaign c = campaignRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        if (!immediate && c.getScheduledAt() != null && c.getScheduledAt().isAfter(LocalDateTime.now())) {
            c.setStatus("SCHEDULED");
            campaignRepository.save(c);
            return toDto(c);
        }

        // evaluate audience -> get phone numbers
        List<com.restaurant.waitlist.backend.repository.CustomerAggregation> agg = waitlistRepository.aggregateCustomers(c.getRestaurantId());
        List<String> recipients = agg.stream().map(a -> a.getContact()).distinct().filter(x -> x != null && !x.isBlank()).collect(Collectors.toList());

        // apply audience filters simple
        if ("RECENT_30D".equalsIgnoreCase(c.getAudience())) {
            LocalDate cutoff = LocalDate.now().minusDays(30);
            recipients = agg.stream().filter(a -> a.getLastVisit() != null && a.getLastVisit().isAfter(cutoff)).map(a -> a.getContact()).distinct().collect(Collectors.toList());
        } else if ("LAPSED_30D".equalsIgnoreCase(c.getAudience())) {
            LocalDate cutoff = LocalDate.now().minusDays(30);
            recipients = agg.stream().filter(a -> a.getLastVisit() == null || a.getLastVisit().isBefore(cutoff)).map(a -> a.getContact()).distinct().collect(Collectors.toList());
        } else if ("GOLD_PLATINUM".equalsIgnoreCase(c.getAudience())) {
            recipients = agg.stream().filter(a -> a.getVisits() != null && a.getVisits() > 4).map(a -> a.getContact()).distinct().collect(Collectors.toList());
        }

        int sent = 0;
        for (String to : recipients) {
            try {
                String message = c.getMessage();
                if ((message == null || message.isBlank()) && c.getTemplateId() != null) {
                    message = "";
                }
                if (message != null && !message.isBlank()) {
                    smsService.sendSms(to, message);
                    sent++;
                }
            } catch (Exception ignored) {
            }
        }

        c.setSentCount((c.getSentCount() == null ? 0 : c.getSentCount()) + sent);
        c.setReach(recipients.size());
        c.setStatus("ACTIVE");
        campaignRepository.save(c);

        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(c.getRestaurantId() != null ? c.getRestaurantId() : 0L)
                .action("PUBLISH_CAMPAIGN")
                .details("Published campaign id=" + c.getId() + " sent=" + sent + " reach=" + recipients.size())
                .build());

        return toDto(c);
    }

    private CampaignResponse toDto(Campaign c) {
        return CampaignResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .channel(c.getChannel())
                .audience(c.getAudience())
                .templateId(c.getTemplateId())
                .message(c.getMessage())
                .restaurantId(c.getRestaurantId())
                .locationId(c.getRestaurantId())
                .scheduledAt(c.getScheduledAt())
                .status(c.getStatus())
                .sentCount(c.getSentCount())
                .reach(c.getReach())
                .redemptions(c.getRedemptions())
                .revenueInfluenced(c.getRevenueInfluenced())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
