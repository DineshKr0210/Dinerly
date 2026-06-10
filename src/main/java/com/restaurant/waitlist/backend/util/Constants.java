package com.restaurant.waitlist.backend.util;

public class Constants {
    
    public static final String JWT_SECRET = "your-secret-key-change-this-in-production";
    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours
    public static final long PASSWORD_RESET_EXPIRATION_MS = 3600000; // 1 hour
    
    public static final class Roles {
        public static final String GUEST = "GUEST";
        public static final String RESTAURANT = "RESTAURANT";
        public static final String ADMIN = "ADMIN";
    }
    
    public static final class WaitlistStatus {
        public static final String PENDING = "PENDING";
        public static final String WAITING = "WAITING";
        public static final String NOTIFIED = "NOTIFIED";
        public static final String SEATED = "SEATED";
        public static final String CANCELLED = "CANCELLED";
        public static final String NO_SHOW = "NO_SHOW";
    }
    
    public static final class Messages {
        public static final String LOGIN_SUCCESS = "Login successful";
        public static final String LOGIN_FAILED = "Invalid credentials";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String WAITLIST_JOINED = "Successfully joined waitlist";
        public static final String WAITLIST_REMOVED = "Removed from waitlist";
        public static final String GUEST_ADDED = "Guest added to waitlist";
        public static final String GUEST_SEATED = "Guest seated successfully";
        public static final String GUEST_NOTIFIED = "Guest notified successfully";
        public static final String PASSWORD_RESET_EMAIL_SENT = "Password reset email sent successfully";
        public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully";
        public static final String FEEDBACK_SUBMITTED = "Feedback submitted successfully";
    }
}

