package com.restaurant.waitlist.backend.util;

import java.util.Random;

/**
 * Utility class for generating 6-digit redemption codes
 */
public class RedemptionCodeGenerator {
    private static final Random random = new Random();

    /**
     * Generate a random 6-digit redemption code
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

    private RedemptionCodeGenerator() {
        // Private constructor to prevent instantiation
    }
}
