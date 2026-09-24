package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.CampaignRequest;
import com.restaurant.waitlist.backend.dto.response.admin.CampaignResponse;
import com.restaurant.waitlist.backend.dto.response.admin.MarketingSummaryResponse;
import com.restaurant.waitlist.backend.entity.Campaign;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.SmsService;
import com.restaurant.waitlist.backend.service.SmsTemplateService;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.AuditLogService;
import com.restaurant.waitlist.backend.service.admin.AdminCampaignService;
import com.restaurant.waitlist.backend.service.audience.AudienceFilterResolver;
import com.restaurant.waitlist.backend.util.RedemptionCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCampaignServiceImpl implements AdminCampaignService {

    private final CampaignRepository campaignRepository;
    private final RedemptionRepository redemptionRepository;
    private final WaitlistRepository waitlistRepository;
    private final RestaurantRepository restaurantRepository;
    private final SmsService smsService;
    private final SmsTemplateService smsTemplateService;
    private final AdminLocationAccessService adminLocationAccessService;
    private final AuditLogService auditLogService;

    private static final Pattern REDEMPTION_CODE_PATTERN = Pattern.compile("^[A-Z0-9-]{3,20}$");

    @Override
    @Transactional
    public CampaignResponse createCampaign(CampaignRequest req) {
        adminLocationAccessService.assertAccess(req.getRestaurantId());
        String customCode = normalizeAndValidateCode(req.getRedemptionCode());
        if (customCode != null && campaignRepository.existsByRedemptionCode(customCode)) {
            throw new IllegalArgumentException("Redemption code '" + customCode + "' is already in use");
        }
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
                .redemptionCode(customCode)
                .revenueInfluenced(null)
                .build();
        Campaign saved = campaignRepository.save(c);
        auditLogService.log(req.getRestaurantId() != null ? req.getRestaurantId() : 0L, "CREATE_CAMPAIGN", "Campaign created: " + saved.getName());
        return toDto(saved);
    }

    @Override
    @Transactional
    public CampaignResponse updateCampaign(Long id, CampaignRequest req) {
        Campaign c = campaignRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        adminLocationAccessService.assertAccess(c.getRestaurantId());
        adminLocationAccessService.assertAccess(req.getRestaurantId());
        c.setName(req.getName());
        c.setChannels(req.getChannel());
        c.setAudience(req.getAudience());
        c.setTemplateId(req.getTemplateId());
        c.setMessage(req.getMessage());
        c.setRestaurantId(req.getRestaurantId());
        c.setScheduledAt(req.getScheduledAt());
        c.setEndDate(req.getEndDate());
        if (req.getHasRedemptionCode() != null) c.setHasRedemptionCode(req.getHasRedemptionCode());
        if (req.getRedemptionCode() != null) {
            String customCode = normalizeAndValidateCode(req.getRedemptionCode());
            if (customCode != null && !customCode.equals(c.getRedemptionCode()) && campaignRepository.existsByRedemptionCode(customCode)) {
                throw new IllegalArgumentException("Redemption code '" + customCode + "' is already in use");
            }
            c.setRedemptionCode(customCode);
        }
        if (req.getScheduledAt() != null) c.setStatus("SCHEDULED");
        Campaign saved = campaignRepository.save(c);
        auditLogService.log(c.getRestaurantId() != null ? c.getRestaurantId() : 0L, "UPDATE_CAMPAIGN", "Campaign updated: " + saved.getName());
        return toDto(saved);
    }

    @Override
    public Page<CampaignResponse> listCampaigns(Pageable pageable) {
        return listCampaigns(pageable, null, null, null);
    }

    @Override
    public Page<CampaignResponse> listCampaigns(Pageable pageable, Long locationId, String status, String channel) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
        String normalizedChannel = channel == null || channel.isBlank() ? null : channel.trim().toUpperCase(Locale.ROOT);

        Page<Campaign> p;
        if (normalizedStatus != null && normalizedChannel != null) {
            p = campaignRepository.findByRestaurantIdInAndStatusAndChannel(restaurantIds, normalizedStatus, normalizedChannel, pageable);
        } else if (normalizedStatus != null) {
            p = campaignRepository.findByRestaurantIdInAndStatus(restaurantIds, normalizedStatus, pageable);
        } else if (normalizedChannel != null) {
            p = campaignRepository.findByRestaurantIdInAndChannel(restaurantIds, normalizedChannel, pageable);
        } else {
            p = campaignRepository.findByRestaurantIdIn(restaurantIds, pageable);
        }

        List<CampaignResponse> items = p.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(items, pageable, p.getTotalElements());
    }

    @Override
    public MarketingSummaryResponse getMarketingSummary(Long locationId) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);

        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth lastMonth = currentMonth.minusMonths(1);

        LocalDateTime currentMonthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime currentMonthEnd = currentMonth.atEndOfMonth().plusDays(1).atStartOfDay().minusSeconds(1);
        LocalDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthEnd = lastMonth.atEndOfMonth().plusDays(1).atStartOfDay().minusSeconds(1);

        List<Campaign> campaignsThisMonth = campaignRepository.findByRestaurantIdInAndCreatedAtBetween(restaurantIds, currentMonthStart, currentMonthEnd);

        List<Campaign> campaignsLastMonth = campaignRepository.findByRestaurantIdInAndCreatedAtBetween(restaurantIds, lastMonthStart, lastMonthEnd);
        
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
        adminLocationAccessService.assertAccess(c.getRestaurantId());
        return toDto(c);
    }

    @Override
    @Transactional
    public CampaignResponse publishCampaign(Long id, boolean immediate) throws Exception {
        Campaign c = campaignRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        adminLocationAccessService.assertAccess(c.getRestaurantId());
        if (!immediate && c.getScheduledAt() != null && c.getScheduledAt().isAfter(LocalDateTime.now())) {
            c.setStatus("SCHEDULED");
            campaignRepository.save(c);
            return toDto(c);
        }

        // evaluate audience -> get phone numbers. A campaign with no specific
        // restaurant targets the publishing admin's whole franchise group,
        // never every restaurant in the system.
        List<Long> targetRestaurantIds = adminLocationAccessService.resolveRestaurantIds(c.getRestaurantId());
        List<com.restaurant.waitlist.backend.repository.CustomerAggregation> agg = waitlistRepository.aggregateCustomersByRestaurantIds(targetRestaurantIds);

        // Only send marketing SMS to guests who consented when joining the waitlist.
        List<com.restaurant.waitlist.backend.repository.CustomerAggregation> consentingAgg = agg.stream()
                .filter(a -> Boolean.TRUE.equals(a.getMarketingSmsConsent()))
                .collect(Collectors.toList());

        // ✅ Use strategy pattern for audience filtering (replaces hardcoded if-else)
        List<String> recipients = AudienceFilterResolver.resolve(c.getAudience(), consentingAgg);

        // *** SHARED COUPON CODE: one code for the whole campaign, identical for every recipient ***
        if (Boolean.TRUE.equals(c.getHasRedemptionCode()) && (c.getRedemptionCode() == null || c.getRedemptionCode().isBlank())) {
            String restaurantName = c.getRestaurantId() != null
                    ? restaurantRepository.findById(c.getRestaurantId()).map(Restaurant::getName).orElse("")
                    : "";
            String baseCode = RedemptionCodeGenerator.generateCampaignCode(restaurantName, c.getName());
            c.setRedemptionCode(ensureUniqueCampaignCode(baseCode));
        }

        // ✅ DEDUPLICATE RECIPIENTS to prevent duplicate SMS to same phone number
        java.util.Set<String> uniqueRecipients = new java.util.LinkedHashSet<>(recipients);
        
        int sent = 0;
        for (String to : uniqueRecipients) {
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
                
                // *** APPEND THE SHARED COUPON CODE (identical for every recipient) ***
                if (Boolean.TRUE.equals(c.getHasRedemptionCode()) && c.getRedemptionCode() != null) {
                    message = message + "\n\nUse code: " + c.getRedemptionCode();
                }
                
                if (message != null && !message.isBlank()) {
                    smsService.sendSms(to, message);
                    sent++;
                }
            } catch (Exception ex) {
                log.warn("Failed to send SMS to {}: {}", to, ex.getMessage());
            }
        }

        c.setSentCount((c.getSentCount() == null ? 0 : c.getSentCount()) + sent);
        c.setReach(uniqueRecipients.size());  // Use unique recipients count for accurate reach
        c.setStatus("ACTIVE");
        campaignRepository.save(c);

        auditLogService.log(c.getRestaurantId() != null ? c.getRestaurantId() : 0L, "PUBLISH_CAMPAIGN",
                "Published campaign id=" + c.getId() + " sent=" + sent + " reach=" + uniqueRecipients.size());

        return toDto(c);
    }

    /**
     * A generated code is derived from the campaign name/restaurant name, so a collision
     * only happens when two campaigns would otherwise produce the identical code.
     */
    private String ensureUniqueCampaignCode(String baseCode) {
        String code = baseCode;
        int suffix = 2;
        while (campaignRepository.existsByRedemptionCode(code) && suffix < 20) {
            code = baseCode + suffix;
            suffix++;
        }
        return code;
    }

    private String normalizeAndValidateCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        String normalized = rawCode.trim().toUpperCase(Locale.ROOT);
        if (!REDEMPTION_CODE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Redemption code must be 3-20 characters (letters, numbers, hyphens only)");
        }
        return normalized;
    }

    private CampaignResponse toDto(Campaign c) {
        long actualRedemptions = redemptionRepository.countByCampaignId(c.getId());

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
                .codesGenerated(c.getRedemptionCode() != null ? 1 : 0)
                .hasRedemptionCode(c.getHasRedemptionCode())
                .redemptionCode(c.getRedemptionCode())
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
