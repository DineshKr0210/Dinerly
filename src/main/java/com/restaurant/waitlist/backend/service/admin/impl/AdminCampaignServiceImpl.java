package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.CampaignRequest;
import com.restaurant.waitlist.backend.dto.response.admin.CampaignResponse;
import com.restaurant.waitlist.backend.dto.response.admin.MarketingSummaryResponse;
import com.restaurant.waitlist.backend.entity.Campaign;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.SmsService;
import com.restaurant.waitlist.backend.service.SmsTemplateService;
import com.restaurant.waitlist.backend.service.admin.AdminCampaignService;
import com.restaurant.waitlist.backend.service.audience.AudienceFilterResolver;
import com.restaurant.waitlist.backend.util.RedemptionCodeGenerator;
import com.restaurant.waitlist.backend.entity.Redemption;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCampaignServiceImpl implements AdminCampaignService {

    private final CampaignRepository campaignRepository;
    private final RedemptionRepository redemptionRepository;
    private final WaitlistRepository waitlistRepository;
    private final SmsService smsService;
    private final SmsTemplateService smsTemplateService;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public CampaignResponse createCampaign(CampaignRequest req) {
        Campaign c = Campaign.builder()
                .name(req.getName())
                .channels(req.getChannel())
                .audience(req.getAudience())
                .templateId(req.getTemplateId())
                .message(req.getMessage())
                .restaurantId(req.getRestaurantId())
                .scheduledAt(req.getScheduledAt())
                .endDate(req.getEndDate())
                .status(req.getScheduledAt() != null ? "SCHEDULED" : "DRAFT")
                .sentCount(0)
                .reach(0)
                .redemptions(0)
                .hasRedemptionCode(req.getHasRedemptionCode() != null ? req.getHasRedemptionCode() : false)
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
        c.setChannels(req.getChannel());
        c.setAudience(req.getAudience());
        c.setTemplateId(req.getTemplateId());
        c.setMessage(req.getMessage());
        c.setRestaurantId(req.getRestaurantId());
        c.setScheduledAt(req.getScheduledAt());
        c.setEndDate(req.getEndDate());
        if (req.getHasRedemptionCode() != null) c.setHasRedemptionCode(req.getHasRedemptionCode());
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
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth lastMonth = currentMonth.minusMonths(1);
        
        LocalDateTime currentMonthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime currentMonthEnd = currentMonth.atEndOfMonth().plusDays(1).atStartOfDay().minusSeconds(1);
        LocalDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthEnd = lastMonth.atEndOfMonth().plusDays(1).atStartOfDay().minusSeconds(1);
        
        List<Campaign> campaignsThisMonth = (locationId != null)
                ? campaignRepository.findByRestaurantIdAndDateRange(locationId, currentMonthStart, currentMonthEnd)
                : campaignRepository.findByDateRange(currentMonthStart, currentMonthEnd);
        
        List<Campaign> campaignsLastMonth = (locationId != null)
                ? campaignRepository.findByRestaurantIdAndDateRange(locationId, lastMonthStart, lastMonthEnd)
                : campaignRepository.findByDateRange(lastMonthStart, lastMonthEnd);
        
        long activeCampaignsThisMonth = campaignsThisMonth.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();
        long activeCampaignsLastMonth = campaignsLastMonth.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();

        long guestsReachedThisMonth = campaignsThisMonth.stream()
                .filter(c -> c.getReach() != null)
                .mapToLong(Campaign::getReach)
                .sum();

        long redemptionsThisMonth = campaignsThisMonth.stream()
                .mapToLong(c -> redemptionRepository.countByCampaignId(c.getId()))
                .sum();
        
        long redemptionsLastMonth = campaignsLastMonth.stream()
                .mapToLong(c -> redemptionRepository.countByCampaignId(c.getId()))
                .sum();

        long spendThisMonth = campaignsThisMonth.stream()
                .filter(c -> c.getRevenueInfluenced() != null)
                .map(c -> c.getRevenueInfluenced())
                .filter(r -> r != null)
                .mapToLong(BigDecimal::longValue)
                .sum();
        
        long spendLastMonth = campaignsLastMonth.stream()
                .filter(c -> c.getRevenueInfluenced() != null)
                .map(c -> c.getRevenueInfluenced())
                .filter(r -> r != null)
                .mapToLong(BigDecimal::longValue)
                .sum();
        
        double activeCampaignsTrend = calculatePercentChange(activeCampaignsLastMonth, activeCampaignsThisMonth);
        double redemptionsTrend = calculatePercentChange(redemptionsLastMonth, redemptionsThisMonth);
        double spendTrend = calculatePercentChange(spendLastMonth, spendThisMonth);

        return MarketingSummaryResponse.builder()
                .activeCampaigns(activeCampaignsThisMonth)
                .activeCampaignsTrendPercent(activeCampaignsTrend)
                .guestsReached(guestsReachedThisMonth)
                .guestsReachedPercent(guestsReachedThisMonth > 0 ? 100.0 : 0.0)
                .redemptions(redemptionsThisMonth)
                .redemptionsLastMonth(redemptionsLastMonth)
                .redemptionsTrendPercent(redemptionsTrend)
                .spendThisMonth(spendThisMonth)
                .spendTrendPercent(spendTrend)
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
        
        // ✅ Use strategy pattern for audience filtering (replaces hardcoded if-else)
        List<String> recipients = AudienceFilterResolver.resolve(c.getAudience(), agg);

        // *** CONDITIONAL CODE GENERATION ***
        java.util.Map<String, String> phoneToCode = new java.util.HashMap<>();
        int codesGenerated = 0;
        
        if (Boolean.TRUE.equals(c.getHasRedemptionCode())) {
            // ✅ OFFER CAMPAIGN: Generate codes for each recipient
            LocalDateTime codeExpiresAt = LocalDateTime.now().plusHours(24); // 24-hour TTL
            
            for (String phone : recipients) {
                try {
                    // Generate unique 6-digit code
                    String code;
                    int attempts = 0;
                    do {
                        code = RedemptionCodeGenerator.generate();
                        attempts++;
                        if (attempts > 20) break; // Prevent infinite loop
                    } while (redemptionRepository.existsByRedemptionCode(code));
                    
                    // Create redemption record with campaign_id FK
                    Redemption redemption = Redemption.builder()
                            .campaign(c)
                            .redemptionCode(code)
                            .guestPhone(phone)
                            .status(Redemption.RedemptionStatus.GENERATED)
                            .codeExpiresAt(codeExpiresAt)
                            .restaurantId(c.getRestaurantId())
                            .build();
                    
                    redemptionRepository.save(redemption);
                    phoneToCode.put(phone, code);
                    codesGenerated++;
                } catch (Exception ex) {
                    // Log but continue with next recipient
                }
            }
        }

        int sent = 0;
        for (String to : recipients) {
            try {
                String message = c.getMessage();
                if ((message == null || message.isBlank()) && c.getTemplateId() != null) {
                    try {
                        if (c.getRestaurantId() != null) {
                            message = smsTemplateService.formatMessageForRestaurantByTemplateId(c.getRestaurantId(), c.getTemplateId(), java.util.Map.of());
                        } else {
                            message = smsTemplateService.formatMessageByTemplateId(c.getTemplateId(), java.util.Map.of());
                        }
                    } catch (Exception ignored) {
                        message = "";
                    }
                }
                
                // *** PERSONALIZE MESSAGE WITH CODE IF GENERATED ***
                if (Boolean.TRUE.equals(c.getHasRedemptionCode()) && phoneToCode.containsKey(to)) {
                    String code = phoneToCode.get(to);
                    message = message + "\n\nUse code: " + code;
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
        long actualRedemptions = redemptionRepository.countByCampaignId(c.getId());
        int codesGenerated = Boolean.TRUE.equals(c.getHasRedemptionCode()) 
            ? redemptionRepository.countCodesGeneratedForCampaign(c.getId())
            : 0;
        
        String dateRangeDisplay = "";
        if (c.getScheduledAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
            if (c.getEndDate() != null) {
                dateRangeDisplay = c.getScheduledAt().format(formatter) + " - " + c.getEndDate().format(formatter);
            } else {
                dateRangeDisplay = c.getScheduledAt().format(formatter);
            }
        }
        
        String channelDisplay = parseChannelDisplay(c.getChannels());
        
        return CampaignResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .channels(c.getChannels())
                .channelDisplay(channelDisplay)
                .audience(c.getAudience() != null ? c.getAudience().getCode() : "ALL")
                .templateId(c.getTemplateId())
                .message(c.getMessage())
                .restaurantId(c.getRestaurantId())
                .locationId(c.getRestaurantId())
                .scheduledAt(c.getScheduledAt())
                .endDate(c.getEndDate())
                .dateRangeDisplay(dateRangeDisplay)
                .status(c.getStatus())
                .sentCount(c.getSentCount())
                .reach(c.getReach())
                .redemptions((int) actualRedemptions)
                .codesGenerated(codesGenerated)
                .hasRedemptionCode(c.getHasRedemptionCode())
                .revenueInfluenced(c.getRevenueInfluenced())
                .createdAt(c.getCreatedAt())
                .build();
    }
    
    private String parseChannelDisplay(String channels) {
        if (channels == null || channels.isBlank()) return "";
        if (channels.contains("[")) {
            return channels.replace("[", "").replace("]", "").replace("\"", "").replace(",", " + ");
        }
        return channels;
    }
    
    private double calculatePercentChange(long previous, long current) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return Math.round(((current - previous) / (double) previous * 100.0) * 10.0) / 10.0;
    }
}
