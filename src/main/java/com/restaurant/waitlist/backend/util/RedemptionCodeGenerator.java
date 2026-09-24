package com.restaurant.waitlist.backend.util;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for generating redemption codes.
 */
public class RedemptionCodeGenerator {
    private static final Random random = new Random();

    /**
     * Generate a random 6-digit redemption code (offer/reward codes).
     * @return 6-digit code as String (e.g., "514527")
     */
    public static String generate() {
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Generate multiple unique codes
     * @param count Number of codes to generate
     * @return Array of unique codes
     */
    public static String[] generateMultiple(int count) {
        String[] codes = new String[count];
        for (int i = 0; i < count; i++) {
            codes[i] = generate();
        }
        return codes;
    }

    // ------------------------------------------------------------------
    // Shared campaign coupon codes (e.g. "BROTPIZZA50"), one per campaign,
    // sent identically to every recipient.
    // ------------------------------------------------------------------

    private static final Set<String> FILLER_WORDS = Set.of(
            "off", "on", "for", "at", "the", "a", "an", "of", "with", "get", "buy",
            "deal", "sale", "special", "promo", "offer", "flat", "and", "to", "your", "our", "is");

    private static final List<String> RESTAURANT_SUFFIXES = List.of(
            "restaurant", "cafe", "kitchen", "grill", "bar", "diner", "bistro", "eatery", "house", "pizzeria");

    private static final Pattern PERCENT_PATTERN = Pattern.compile("(\\d+)\\s*%");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("[₹$](\\d+)");
    private static final Pattern NON_ALPHA = Pattern.compile("[^A-Za-z]+");

    private static final int RESTAURANT_PREFIX_LENGTH = 4;
    private static final int KEYWORD_MAX_LENGTH = 6;
    private static final int MAX_CODE_LENGTH = 14;

    /**
     * Derives a shared coupon code from the restaurant name and campaign name, e.g.
     * ("Brothers Cafe", "50% off on pizza") -> "BROTPIZZA50".
     * Falls back to dropping the keyword, then to a random suffix, if nothing usable is found.
     */
    public static String generateCampaignCode(String restaurantName, String campaignName) {
        String restaurantPart = shortenRestaurantName(restaurantName);
        String number = extractNumber(campaignName);
        String keyword = extractKeyword(campaignName);

        String withKeyword = restaurantPart + keyword + number;
        if (!withKeyword.isBlank() && withKeyword.length() <= MAX_CODE_LENGTH) {
            return withKeyword;
        }

        String withoutKeyword = restaurantPart + number;
        if (!withoutKeyword.isBlank()) {
            return withoutKeyword.length() <= MAX_CODE_LENGTH ? withoutKeyword : withoutKeyword.substring(0, MAX_CODE_LENGTH);
        }

        return restaurantPart.isBlank() ? "PROMO" + generate().substring(0, 4) : restaurantPart;
    }

    private static String shortenRestaurantName(String restaurantName) {
        if (restaurantName == null || restaurantName.isBlank()) {
            return "";
        }
        String[] words = restaurantName.trim().split("\\s+");
        String candidate = NON_ALPHA.matcher(words[0]).replaceAll("");
        if (RESTAURANT_SUFFIXES.contains(candidate.toLowerCase(Locale.ROOT)) && words.length > 1) {
            // First word is a generic suffix (e.g. "Cafe Aroma") - use the next word instead.
            candidate = NON_ALPHA.matcher(words[1]).replaceAll("");
        }
        candidate = candidate.toUpperCase(Locale.ROOT);
        return candidate.length() > RESTAURANT_PREFIX_LENGTH ? candidate.substring(0, RESTAURANT_PREFIX_LENGTH) : candidate;
    }

    private static String extractNumber(String campaignName) {
        if (campaignName == null) {
            return "";
        }
        Matcher percentMatcher = PERCENT_PATTERN.matcher(campaignName);
        if (percentMatcher.find()) {
            return percentMatcher.group(1);
        }
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(campaignName);
        if (amountMatcher.find()) {
            return amountMatcher.group(1);
        }
        return "";
    }

    private static String extractKeyword(String campaignName) {
        if (campaignName == null || campaignName.isBlank()) {
            return "";
        }
        String withoutPercent = PERCENT_PATTERN.matcher(campaignName).replaceAll(" ");
        String withoutAmount = AMOUNT_PATTERN.matcher(withoutPercent).replaceAll(" ");

        StringBuilder keyword = new StringBuilder();
        for (String word : withoutAmount.split("\\s+")) {
            String cleaned = NON_ALPHA.matcher(word).replaceAll("");
            if (cleaned.isBlank() || FILLER_WORDS.contains(cleaned.toLowerCase(Locale.ROOT))) {
                continue;
            }
            keyword.append(cleaned.toUpperCase(Locale.ROOT));
            if (keyword.length() >= KEYWORD_MAX_LENGTH) {
                break;
            }
        }
        return keyword.length() > KEYWORD_MAX_LENGTH ? keyword.substring(0, KEYWORD_MAX_LENGTH) : keyword.toString();
    }

    private RedemptionCodeGenerator() {
    }
}
