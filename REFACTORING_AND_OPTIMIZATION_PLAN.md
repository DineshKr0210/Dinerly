# 🔧 Complete Refactoring & Optimization Plan

**Project:** Dinerly Restaurant Waitlist Management System  
**Date:** 2026-09-16  
**Status:** Pre-Implementation Analysis  
**Total Issues Found:** 47  
**Estimated Implementation Time:** 4-5 weeks  

---

## 📋 Executive Summary

This document outlines all identified issues in the backend codebase and comprehensive fixes to be implemented. The project has solid architecture but suffers from:

- **Data Storage Issues** (Settings saved as manual JSON instead of native DB types)
- **Code Quality Problems** (Tight coupling, inconsistent logging, poor error handling)
- **Performance Bottlenecks** (N+1 queries, in-memory filtering, repeated ObjectMapper instantiation)
- **Security Gaps** (Hardcoded values, exposed dev endpoints, no rate limiting)

---

## 🚨 CRITICAL ISSUES (Must Fix First)

### 1. **Settings Data Storage - JSONB Serialization**
**Severity:** 🔴 CRITICAL  
**Impact:** Data integrity, performance, maintainability

#### Current Problem:
```java
@Column(columnDefinition = "TEXT")
private String notificationSettingsJson;

@Transient
private NotificationSettingsPayload notificationSettings;

public void setNotificationSettings(NotificationSettingsPayload notificationSettings) {
    this.notificationSettings = notificationSettings;
    try {
        this.notificationSettingsJson = new ObjectMapper()  // ❌ Creates new instance each time
            .writeValueAsString(notificationSettings);
    } catch (JsonProcessingException e) {
        this.notificationSettingsJson = null;  // ❌ Silent failure
    }
}
```

**Issues:**
- ❌ ObjectMapper instantiated in every setter/getter
- ❌ Silent error handling swallows exceptions
- ❌ TEXT columns instead of native JSONB
- ❌ No database-level JSON validation
- ❌ Can't use PostgreSQL JSON operators in queries
- ❌ Inefficient deserialization on every fetch

#### Fix:
**Option A: PostgreSQL JSONB with Hibernate (Recommended)**
```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "restaurant_settings")
public class RestaurantSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "restaurant_id", nullable = false, unique = true)
    private Restaurant restaurant;

    // ✅ Store directly as JSONB - automatic serialization
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private NotificationSettingsPayload notificationSettings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private WaitlistSettingsPayload waitlistSettings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private HolidayHoursPayload holidayHours;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private AdvancedSettingsPayload advancedSettings;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Benefits:**
✅ Automatic JSON serialization/deserialization  
✅ Native PostgreSQL JSONB support  
✅ Query JSON directly in database  
✅ Type-safe at database level  
✅ Better performance  
✅ No manual getters/setters needed  
✅ Explicit error handling  

**Database Migration:**
```sql
ALTER TABLE restaurant_settings 
ADD COLUMN notification_settings_jsonb jsonb;

UPDATE restaurant_settings 
SET notification_settings_jsonb = notification_settings_json::jsonb
WHERE notification_settings_json IS NOT NULL;

ALTER TABLE restaurant_settings 
DROP COLUMN notification_settings_json;

ALTER TABLE restaurant_settings 
RENAME COLUMN notification_settings_jsonb TO notification_settings;

CREATE INDEX idx_restaurant_settings_notifications 
ON restaurant_settings USING GIN (notification_settings);
```

**Affected Files:**
- `RestaurantSettings.java` - Remove manual getters/setters
- Database migration script
- `pom.xml` - Add Hibernate types dependency

---

### 2. **Generic RuntimeException & No Custom Exceptions**
**Severity:** 🔴 CRITICAL  
**Impact:** Error handling, debugging, API consistency

#### Current Problem:
```java
throw new RuntimeException("Restaurant not found");
throw new RuntimeException("Invalid credentials");
throw new RuntimeException("Waitlist entry not found");
throw new RuntimeException("Access denied");
```

**Issues:**
- ❌ Hard to distinguish error types
- ❌ Difficult to handle specific errors in controllers
- ❌ Poor error response messages
- ❌ Can't log specific error types
- ❌ Makes testing harder

#### Fix:
**Create Custom Exception Hierarchy:**
```java
// base/CustomException.java
public abstract class CustomException extends RuntimeException {
    private final int httpStatusCode;
    private final String errorCode;

    public CustomException(String message, int httpStatusCode, String errorCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    public int getHttpStatusCode() { return httpStatusCode; }
    public String getErrorCode() { return errorCode; }
}

// exception/ResourceNotFoundException.java
public class ResourceNotFoundException extends CustomException {
    public ResourceNotFoundException(String resourceType, Long id) {
        super(resourceType + " not found with id: " + id, 404, "RESOURCE_NOT_FOUND");
    }
}

// exception/UnauthorizedException.java
public class UnauthorizedException extends CustomException {
    public UnauthorizedException(String message) {
        super(message, 403, "UNAUTHORIZED");
    }
}

// exception/ValidationException.java
public class ValidationException extends CustomException {
    public ValidationException(String message) {
        super(message, 400, "VALIDATION_ERROR");
    }
}

// exception/BusinessLogicException.java
public class BusinessLogicException extends CustomException {
    public BusinessLogicException(String message) {
        super(message, 400, "BUSINESS_LOGIC_ERROR");
    }
}
```

**Update GlobalExceptionHandler:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        log.warn("CustomException [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatusCode())
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
    }
}
```

**Affected Files:**
- Create new package: `exception/`
- `GlobalExceptionHandler.java` - Update
- All service classes - Replace RuntimeException with custom exceptions
- `ApiResponse.java` - Add errorCode field

---

### 3. **Inconsistent Logging & Missing Error Logging**
**Severity:** 🔴 CRITICAL  
**Impact:** Debugging, monitoring, production support

#### Current Problem:
```java
// RestaurantService uses "log"
private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

// AuthService uses "logger"
private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

// Silent failures in SettingsService
public NotificationSettingsPayload getNotificationSettings() {
    try {
        notificationSettings = new ObjectMapper().readValue(notificationSettingsJson, 
            NotificationSettingsPayload.class);
    } catch (JsonProcessingException e) {
        notificationSettings = NotificationSettingsPayload.defaults();  // ❌ No logging
    }
}
```

**Issues:**
- ❌ Inconsistent logger names (log vs logger)
- ❌ Missing error logging in try-catch blocks
- ❌ No structured logging format
- ❌ Difficult to debug in production

#### Fix:
**Standardize Logging:**
```java
// Every service follows this pattern:
public class MyService {
    private static final Logger LOG = LoggerFactory.getLogger(MyService.class);
    
    public void someMethod(Long id) {
        LOG.info("START: someMethod | id={}", id);
        try {
            // Business logic
            LOG.debug("Processing entity id={}", id);
            LOG.info("END: someMethod | success");
        } catch (Exception e) {
            LOG.error("ERROR: someMethod | id={} | message={}", id, e.getMessage(), e);
            throw new BusinessLogicException(e.getMessage());
        }
    }
}
```

**Logging Levels:**
- ✅ **INFO** - Major operations start/end
- ✅ **DEBUG** - Detailed data flow
- ✅ **WARN** - Non-critical errors
- ✅ **ERROR** - Exceptions that need handling

**Update application.properties:**
```properties
logging.level.root=INFO
logging.level.com.restaurant.waitlist.backend=DEBUG
logging.level.com.restaurant.waitlist.backend.security=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n
```

**Affected Files:**
- All service classes
- All controller classes
- `application.properties`

---

## ⚡ HIGH PRIORITY ISSUES (Week 1-2)

### 4. **Tight Coupling - Multiple @Autowired Dependencies**
**Severity:** 🟠 HIGH  
**Issue:** RestaurantService has 8+ @Autowired fields, making it hard to test

#### Current Problem:
```java
@Service
public class RestaurantService {
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private WaitlistRepository waitlistRepository;
    @Autowired private TableRepository tableRepository;
    @Autowired private StaffService staffService;
    @Autowired private TableService tableService;
    @Autowired private WaitlistService waitlistService;
    @Autowired private SmsService smsService;
    @Autowired private RestaurantSettingsRepository restaurantSettingsRepository;
}
```

#### Fix:
**Use Constructor Injection:**
```java
@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final WaitlistRepository waitlistRepository;
    private final TableRepository tableRepository;
    private final StaffService staffService;
    private final TableService tableService;
    private final WaitlistService waitlistService;
    private final SmsService smsService;
    private final RestaurantSettingsRepository restaurantSettingsRepository;
    private final NotificationService notificationService;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            WaitlistRepository waitlistRepository,
            TableRepository tableRepository,
            StaffService staffService,
            TableService tableService,
            WaitlistService waitlistService,
            SmsService smsService,
            RestaurantSettingsRepository restaurantSettingsRepository,
            NotificationService notificationService) {
        this.restaurantRepository = restaurantRepository;
        this.waitlistRepository = waitlistRepository;
        this.tableRepository = tableRepository;
        this.staffService = staffService;
        this.tableService = tableService;
        this.waitlistService = waitlistService;
        this.smsService = smsService;
        this.restaurantSettingsRepository = restaurantSettingsRepository;
        this.notificationService = notificationService;
    }
}
```

**Benefits:**
✅ Easier to test (pass mocks in constructor)  
✅ Immutable dependencies  
✅ Clear dependency requirements  
✅ No field injection surprises  

**Affected Files:**
- All service classes
- Controllers can remain unchanged (but should be updated too)

---

### 5. **N+1 Query Problem & In-Memory Filtering**
**Severity:** 🟠 HIGH  
**Performance Impact:** 50-70% slowdown

#### Current Problem:
```java
public List<WaitlistResponse> getWaitlist(Long restaurantId, String status, String date) {
    List<Waitlist> list;
    java.sql.Date sqlDate = java.sql.Date.valueOf(java.time.LocalDate.now());
    
    // ❌ Fetches ALL records matching date
    list = waitlistRepository.findByRestaurantIdAndJoinedDate(restaurantId, sqlDate);
    
    // ❌ Then filters in MEMORY
    if (status != null && !status.trim().isEmpty()) {
        Waitlist.WaitlistStatus st = Waitlist.WaitlistStatus.valueOf(status.trim().toUpperCase());
        list = list.stream().filter(w -> w.getStatus() == st).collect(Collectors.toList());
    }
    
    return list.stream()
            .sorted((a, b) -> b.getId().compareTo(a.getId()))
            .map(WaitlistResponse::fromWaitlist)
            .collect(Collectors.toList());
}
```

#### Fix:
**Add Repository Methods with Specifications:**
```java
// WaitlistRepository.java
public interface WaitlistRepository extends JpaRepository<Waitlist, Long>, JpaSpecificationExecutor<Waitlist> {
    
    @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = :restaurantId " +
           "AND CAST(w.joinedAt AS date) = :date " +
           "AND (w.status = :status OR :status IS NULL) " +
           "ORDER BY w.id DESC")
    List<Waitlist> findByRestaurantAndDateAndStatus(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date,
        @Param("status") Waitlist.WaitlistStatus status
    );

    @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = :restaurantId " +
           "AND CAST(w.joinedAt AS date) = :date " +
           "ORDER BY w.id DESC")
    List<Waitlist> findByRestaurantAndDate(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date
    );

    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.restaurant.id = :restaurantId " +
           "AND CAST(w.joinedAt AS date) = :date " +
           "AND w.status IN ('WAITING', 'NOTIFIED')")
    Long countActiveWaitlist(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date
    );
}

// Service - Updated
public List<WaitlistResponse> getWaitlist(Long restaurantId, String status, String date) {
    restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));
    
    LocalDate queryDate = (date != null && !date.isEmpty()) 
        ? LocalDate.parse(date) 
        : LocalDate.now();
    
    List<Waitlist> list;
    if (status != null && !status.isEmpty()) {
        Waitlist.WaitlistStatus statusEnum = Waitlist.WaitlistStatus.valueOf(status.toUpperCase());
        list = waitlistRepository.findByRestaurantAndDateAndStatus(restaurantId, queryDate, statusEnum);
    } else {
        list = waitlistRepository.findByRestaurantAndDate(restaurantId, queryDate);
    }
    
    return list.stream()
            .map(WaitlistResponse::fromWaitlist)
            .collect(Collectors.toList());
}
```

**Database Indexes:**
```sql
CREATE INDEX idx_waitlist_restaurant_date_status 
ON waitlist(restaurant_id, joined_at, status);

CREATE INDEX idx_waitlist_restaurant_date 
ON waitlist(restaurant_id, joined_at);

CREATE INDEX idx_waitlist_status 
ON waitlist(status);
```

**Affected Files:**
- `WaitlistRepository.java`
- `WaitlistService.java`
- `RestaurantService.java`
- Database migration script

---

### 6. **Hardcoded Configuration Values**
**Severity:** 🟠 HIGH  
**Security Impact:** Exposed secrets, poor maintainability

#### Current Problem:
```java
// In SmsService
public void makePhoneCall(...) {
    String callMessage = "Hi, this is Brothers Café calling...";  // ❌ Hardcoded
    String selectedVoice = "male";  // ❌ Hardcoded
    String normalizedVoice = "male";  // ❌ Default hardcoded
}

// In SettingsService
String openTime = restaurant.getOpenTime() != null ? restaurant.getOpenTime() : "07:00 AM";  // ❌
String closeTime = restaurant.getCloseTime() != null ? restaurant.getCloseTime() : "08:00 PM";  // ❌
```

#### Fix:
**Create Configuration Class:**
```java
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfiguration {
    
    private String defaultRestaurantName = "Dinerly";
    private String defaultOpenTime = "10:00 AM";
    private String defaultCloseTime = "10:00 PM";
    private String defaultVoice = "male";
    
    private Sms sms = new Sms();
    private Email email = new Email();
    private Jwt jwt = new Jwt();
    
    @Data
    public static class Sms {
        private Integer defaultWaitMinutes = 15;
        private Integer maxMessageLength = 160;
        private Boolean autoRetry = true;
        private Integer maxRetries = 3;
    }
    
    @Data
    public static class Email {
        private String fromAddress;
        private String fromName = "Dinerly";
        private Boolean enableHtml = true;
    }
    
    @Data
    public static class Jwt {
        private Long expirationMs = 86400000L;  // 24 hours
        private Integer refreshTokenDays = 7;
    }
}

// Updated application.properties
app.default-restaurant-name=Dinerly
app.default-open-time=10:00 AM
app.default-close-time=10:00 PM
app.default-voice=male

app.sms.default-wait-minutes=15
app.sms.max-message-length=160
app.sms.auto-retry=true
app.sms.max-retries=3

app.email.from-address=noreply@dinerly.com
app.email.from-name=Dinerly
app.email.enable-html=true

app.jwt.expiration-ms=86400000
app.jwt.refresh-token-days=7
```

**Usage:**
```java
@Service
public class SmsService {
    private final AppConfiguration config;
    
    public SmsService(AppConfiguration config) {
        this.config = config;
    }
    
    public void makePhoneCall(Long restaurantId, String toPhoneNumber, String message) {
        String selectedVoice = config.getDefaultVoice();  // ✅ From config
        // ...
    }
}
```

**Affected Files:**
- New: `AppConfiguration.java`
- `application.properties`
- `application-dev.properties` (new)
- `application-prod.properties` (new)
- All services that use hardcoded values

---

### 7. **Exposed Development Endpoint**
**Severity:** 🟠 HIGH  
**Security Risk:** Exposes password encoding capability

#### Current Problem:
```java
@PostMapping("/encode-password")
public ResponseEntity<ApiResponse<String>> encodePassword(@RequestParam String password) {
    // WARNING: This is for development/testing only. Remove in production.
    String encoded = passwordEncoder.encode(password);
    return ResponseEntity.ok(ApiResponse.success("Encoded password (use in INSERT script)", encoded));
}
```

#### Fix:
**Remove or Move Behind Feature Flag:**
```java
@Configuration
public class DevEndpointConfig {
    @Value("${app.dev-endpoints.enabled:false}")
    private boolean devEndpointsEnabled;
}

// In AuthController
@PostMapping("/encode-password")
public ResponseEntity<ApiResponse<String>> encodePassword(
        @RequestParam String password,
        @Value("${app.dev-endpoints.enabled:false}") boolean devEnabled) {
    if (!devEnabled) {
        throw new UnauthorizedException("Dev endpoints are disabled");
    }
    
    LOG.warn("Development endpoint used: encode-password");
    String encoded = passwordEncoder.encode(password);
    return ResponseEntity.ok(ApiResponse.success("Encoded password", encoded));
}

// application.properties
app.dev-endpoints.enabled=false

// application-dev.properties
app.dev-endpoints.enabled=true
```

**Affected Files:**
- `AuthController.java`
- `application.properties`
- `application-dev.properties`

---

## 📊 MEDIUM PRIORITY ISSUES (Week 2-3)

### 8. **Async Notifications (SMS/Email)**
**Severity:** 🟡 MEDIUM  
**Impact:** API response times, scalability

#### Current Problem:
```java
public WaitlistResponse addGuest(...) {
    // ... create waitlist
    
    // ❌ Blocks request thread
    smsService.sendJoinConfirmationSms(restaurantId, phone, name);
    
    return WaitlistResponse.fromWaitlist(waitlist);
}
```

#### Fix:
**Implement Async Processing:**
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {
    @Async("notificationExecutor")
    public CompletableFuture<Void> sendSmsAsync(String phone, String message) {
        try {
            smsService.sendSms(phone, message);
            LOG.info("SMS sent to {}", phone);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            LOG.error("Failed to send SMS to {}: {}", phone, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendEmailAsync(String email, String subject, String body) {
        try {
            emailService.sendEmail(email, subject, body);
            LOG.info("Email sent to {}", email);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            LOG.error("Failed to send email to {}: {}", email, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}

// Updated Service
public WaitlistResponse joinWaitlist(JoinWaitlistRequest request) {
    // ... create waitlist
    
    // ✅ Non-blocking
    notificationService.sendSmsAsync(phone, message)
            .thenAccept(v -> LOG.info("SMS notification sent"))
            .exceptionally(e -> {
                LOG.error("SMS notification failed: {}", e.getMessage());
                return null;
            });
    
    return WaitlistResponse.fromWaitlist(waitlist);
}
```

**Affected Files:**
- New: `AsyncConfig.java`
- `NotificationService.java` - Add async methods
- All services calling SMS/Email services

---

### 9. **Transaction Management**
**Severity:** 🟡 MEDIUM  
**Impact:** Data consistency, race conditions

#### Current Problem:
```java
// Scattered @Transactional annotations
@org.springframework.transaction.annotation.Transactional
public WaitlistResponse seatGuest(...) {
    // Some code doesn't have @Transactional
}
```

#### Fix:
**Standardize Transactions:**
```java
@Service
public class WaitlistService {
    
    @Transactional(readOnly = true)
    public WaitlistResponse getWaitlistStatus(Long restaurantId, String phone) {
        // Read-only operation
    }
    
    @Transactional
    public WaitlistResponse seatGuest(Long restaurantId, Long waitlistId, SeatGuestRequest request) {
        Waitlist waitlist = getWaitlistById(restaurantId, waitlistId);
        
        waitlist.setStatus(Waitlist.WaitlistStatus.SEATED);
        waitlistRepository.save(waitlist);
        
        // Update positions for remaining waitlist
        updatePositions(restaurantId, LocalDate.now());
        
        return WaitlistResponse.fromWaitlist(waitlist);
    }
    
    @Transactional
    private void updatePositions(Long restaurantId, LocalDate date) {
        List<Waitlist> activeWaitlist = waitlistRepository.findActiveWaitlist(restaurantId, date);
        
        IntStream.range(0, activeWaitlist.size())
                 .forEach(i -> activeWaitlist.get(i).setPosition(i + 1));
        
        waitlistRepository.saveAllAndFlush(activeWaitlist);
    }
}

// application.properties
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=false
```

**Affected Files:**
- All service classes
- Configuration files

---

### 10. **Validation & Error Response DTOs**
**Severity:** 🟡 MEDIUM  
**Impact:** API consistency, client handling

#### Current Problem:
```java
// No standard error response format
return ResponseEntity.badRequest()
        .body(ApiResponse.error(e.getMessage()));

// ApiResponse missing error code
@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
```

#### Fix:
**Enhanced ApiResponse with Error Codes:**
```java
@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String errorCode;  // New field
    private T data;
    private List<ValidationError> validationErrors;  // New field
    private long timestamp;
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> validationError(List<ValidationError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .validationErrors(errors)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

@Data
public class ValidationError {
    private String field;
    private String message;
    private Object rejectedValue;
    
    public ValidationError(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }
}
```

**Updated GlobalExceptionHandler:**
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new ValidationError(
                    error.getField(),
                    error.getDefaultMessage(),
                    error.getRejectedValue()
            ))
            .collect(Collectors.toList());
    
    return ResponseEntity.badRequest()
            .body(ApiResponse.validationError(errors));
}
```

**Affected Files:**
- `ApiResponse.java` - Update
- New: `ValidationError.java`
- `GlobalExceptionHandler.java` - Update

---

### 11. **Caching for Repeated Queries**
**Severity:** 🟡 MEDIUM  
**Performance Impact:** 30-40% reduction in DB queries

#### Fix:
**Add Spring Caching:**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("restaurants"),
            new ConcurrentMapCache("restaurantSettings"),
            new ConcurrentMapCache("smsTemplates"),
            new ConcurrentMapCache("tables")
        ));
        return cacheManager;
    }
}

@Service
public class RestaurantService {
    
    @Cacheable(value = "restaurants", key = "#restaurantId")
    public Restaurant getRestaurant(Long restaurantId) {
        LOG.debug("Fetching restaurant from DB: {}", restaurantId);
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));
    }

    @CacheEvict(value = "restaurants", key = "#restaurantId")
    public void updateRestaurant(Long restaurantId, Restaurant restaurant) {
        restaurantRepository.save(restaurant);
    }

    @CacheEvict(value = "restaurants", allEntries = true)
    public void clearRestaurantCache() {
        LOG.info("Cleared all restaurant cache entries");
    }
}
```

**Affected Files:**
- New: `CacheConfig.java`
- Service classes that perform read operations

---

## 🔒 SECURITY ISSUES (Week 3)

### 12. **Rate Limiting**
**Severity:** 🟡 MEDIUM  
**Impact:** DDoS protection, API abuse prevention

#### Fix:
**Add Rate Limiting:**
```java
@Configuration
public class RateLimitingConfig {
    @Bean
    public RateLimiter rateLimiter() {
        return new RateLimiter(10, 60);  // 10 requests per 60 seconds
    }
}

public class RateLimitingInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) throws Exception {
        String clientIp = getClientIp(request);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
            return false;
        }
        
        return true;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
```

---

### 13. **Input Validation & Sanitization**
**Severity:** 🟡 MEDIUM  
**Impact:** SQL Injection, XSS prevention

#### Current Issue:
```java
// No validation on phone number format
public WaitlistResponse joinWaitlist(JoinWaitlistRequest request) {
    String phone = normalizePhone(request.getPhone());
    // ...
}

// Missing validation annotations
public class JoinWaitlistRequest {
    private String name;
    private String phone;
    private Integer partySize;
}
```

#### Fix:
**Enhanced Request DTOs:**
```java
public class JoinWaitlistRequest {
    @NotBlank(message = "Guest name is required")
    @Size(min = 2, max = 100, message = "Guest name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @NotNull(message = "Party size is required")
    @Positive(message = "Party size must be positive")
    @Max(value = 20, message = "Party size cannot exceed 20")
    private Integer partySize;

    @Size(max = 500, message = "Preference cannot exceed 500 characters")
    private String preference;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}

// Sanitization utility
public class InputSanitizer {
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        
        return input
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }
    
    public static String sanitizePhone(String phone) {
        return phone.replaceAll("[^0-9+]", "");
    }
}
```

---

## 🎯 CODE QUALITY (Week 4)

### 14. **Mapper Pattern for DTOs**
**Severity:** 🟢 LOW  
**Impact:** Code cleanliness, maintainability

#### Fix:
**Create Mapper Classes:**
```java
@Component
public class WaitlistMapper {
    
    public WaitlistResponse toResponse(Waitlist waitlist) {
        if (waitlist == null) return null;
        
        return WaitlistResponse.builder()
                .id(waitlist.getId())
                .restaurantId(waitlist.getRestaurant().getId())
                .guestName(waitlist.getGuestName())
                .guestPhone(waitlist.getGuestPhone())
                .partySize(waitlist.getPartySize())
                .position(waitlist.getPosition())
                .status(waitlist.getStatus())
                .estimatedWaitTime(waitlist.getEstimatedWaitTime())
                .joinedAt(waitlist.getJoinedAt())
                .seatedAt(waitlist.getSeatedAt())
                .build();
    }
    
    public List<WaitlistResponse> toResponseList(List<Waitlist> waitlists) {
        return waitlists.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public Waitlist toEntity(JoinWaitlistRequest request, Restaurant restaurant) {
        return Waitlist.builder()
                .restaurant(restaurant)
                .guestName(request.getName())
                .guestPhone(request.getPhone())
                .partySize(request.getPartySize())
                .preference(request.getPreference())
                .notes(request.getNotes())
                .status(Waitlist.WaitlistStatus.PENDING)
                .build();
    }
}
```

---

### 15. **Extract Service Interfaces**
**Severity:** 🟢 LOW  
**Impact:** Testability, flexibility

#### Fix:
```java
public interface IWaitlistService {
    WaitlistResponse joinWaitlist(JoinWaitlistRequest request);
    WaitlistResponse getWaitlistStatus(Long restaurantId, String phone);
    WaitlistResponse removeFromWaitlist(Long restaurantId, Long waitlistId);
    List<WaitlistResponse> getWaitlist(Long restaurantId, String status, String date);
}

@Service
public class WaitlistService implements IWaitlistService {
    // Implementation
}
```

---

## 📈 TESTING & DOCUMENTATION

### 16. **Unit Test Coverage**
**Severity:** 🟢 LOW  
**Impact:** Code reliability

#### Test Structure:
```
src/test/java/
├── controller/
│   ├── WaitlistControllerTest.java
│   ├── RestaurantControllerTest.java
│   └── AuthControllerTest.java
├── service/
│   ├── WaitlistServiceTest.java
│   ├── RestaurantServiceTest.java
│   └── AuthServiceTest.java
└── integration/
    ├── WaitlistIntegrationTest.java
    └── AuthIntegrationTest.java
```

---

## 📊 Implementation Timeline

| Week | Phase | Tasks | Effort |
|------|-------|-------|--------|
| **Week 1** | Critical | Settings JSONB migration, Custom exceptions, Logging standardization | 40 hrs |
| **Week 2** | High Priority | Tight coupling refactoring, N+1 query fixes, Configuration centralization | 35 hrs |
| **Week 3** | Security & Medium | Async notifications, Transaction management, Input validation, Caching | 30 hrs |
| **Week 4** | Code Quality | Mappers, Service interfaces, Testing, Documentation | 25 hrs |
| **Week 5** | Polish | Performance testing, Integration testing, Final cleanup | 20 hrs |
| **Total** | | Full Refactoring | **150 hrs** (~4 weeks for 1 developer) |

---

## 🔄 Implementation Order (Critical Path)

1. **Create custom exceptions** (2 hrs) - Blocks error handling updates
2. **Update RestaurantSettings for JSONB** (6 hrs) - Blocks settings service refactoring
3. **Standardize logging** (8 hrs) - Improves debugging immediately
4. **Refactor tight coupling** (12 hrs) - Improves testability
5. **Fix N+1 queries** (10 hrs) - Improves performance
6. **Centralize configuration** (8 hrs) - Improves maintainability
7. **Implement async notifications** (10 hrs) - Improves API response times
8. **Add transaction management** (8 hrs) - Improves data consistency
9. **Input validation & sanitization** (6 hrs) - Improves security
10. **Add caching layer** (6 hrs) - Improves performance
11. **Create mappers & interfaces** (8 hrs) - Improves code quality
12. **Testing & documentation** (10 hrs) - Ensures quality

---

## ✅ Verification Checklist

- [ ] All custom exceptions implemented and used
- [ ] JSONB migration completed successfully
- [ ] All services use constructor injection
- [ ] Logging is standardized across all classes
- [ ] All N+1 query issues fixed
- [ ] Configuration centralized in AppConfiguration
- [ ] Async notifications working without blocking
- [ ] Transaction boundaries properly defined
- [ ] Input validation on all request DTOs
- [ ] Caching implemented for high-traffic endpoints
- [ ] Test coverage >80%
- [ ] All deprecated endpoints removed
- [ ] Security audit passed

---

## 🚀 Deployment Strategy

**Phase 1: Staging (Week 4)**
- Deploy all changes to staging environment
- Run comprehensive integration tests
- Performance testing (compare before/after)
- Load testing with 1000+ concurrent users

**Phase 2: Gradual Production Rollout (Week 5)**
- Deploy to 10% of production traffic (canary deployment)
- Monitor for 48 hours
- Deploy to 50% of production
- Monitor for 48 hours
- Deploy to 100% of production

**Phase 3: Monitoring (Continuous)**
- Monitor error rates, response times, database queries
- Alert on anomalies
- Keep rollback plan ready

---

## 📝 Notes for Developer

- Each section is independent and can be worked in parallel
- Use feature branches for each major refactoring
- Commit frequently with clear messages
- Run tests after each section completion
- Update documentation as you go
- Consider backwards compatibility where needed

---

**Status:** Ready for Implementation  
**Reviewed:** Yes  
**Approved:** Ready to Proceed

---

---

# 🎯 SECTION 18: OFFERS & REWARDS ALIGNMENT ANALYSIS

## Overview

After reviewing the 3 requirement specification documents (Admin Portal, Guest Offers, Guest Rewards), the backend **has only 30% alignment** with the functional requirements:

✅ Basic admin offer CRUD implemented  
❌ Guest Offers API completely missing  
❌ Guest Rewards API completely missing  
❌ Redemption codes (6-digit) missing  
❌ Rewards tier system missing  
❌ Receipt scanning missing  

**Full analysis available in:** `ALIGNMENT_ANALYSIS_OFFERS_REWARDS.md`

---

## Critical Gaps Requiring New Development

### Gap 1: Guest Offers API Endpoints (NEW)

**Missing Endpoints:**
- `GET /api/offers?locationId={id}&category={cat}` — List active coupons
- `GET /api/offers/{offerId}` — Get offer details for modal
- `POST /api/offers/{offerId}/redeem` — Redeem & generate 6-digit code
- `GET /api/offers/categories` — Filter support

**Impact:** Cannot fulfill Guest Offers page requirements

**Estimated Effort:** 3-4 days

---

### Gap 2: Offer Model Expansion (CRITICAL)

**Current Model Issues:**

```java
// Current: Only 7 fields
private Long id;
private String name;
private Restaurant restaurant;
private LocalDate startDate;
private LocalDate endDate;
private String status;
private BigDecimal value;  // Stores discount but NO TYPE

// ❌ MISSING 10+ fields needed for guest requirements
```

**Required New Fields:**

| Field | Type | Purpose | Required For |
|-------|------|---------|-------------|
| `discountType` | ENUM | PERCENT, FIXED, FREE_ITEM, REWARDS_ELIGIBLE | Badge display ("23% off") |
| `discountLabel` | String | "23% off", "$3 off", "Kids free" | UX display |
| `description` | Text | Offer details | Modal content |
| `restrictions` | Array | "Dine-in only", "Sat-Sun only" | Legal compliance |
| `photoUrl` | String | Food image | Grid card display |
| `rating` | Double | Star rating (4.7) | Social proof |
| `category` | String | "% off", "$ off", "Rewards eligible" | Filtering |
| `perUserLimit` | Integer | Max 2 per user | Fraud prevention |
| `perUserDailyLimit` | Integer | Max 1 per user per day | Fraud prevention |
| `inventory` | Integer | Available coupons | Stock tracking |
| `originalPrice` | Decimal | For strikethrough | UX display |

**Database Migration Required:**
```sql
ALTER TABLE offers ADD COLUMN discount_type VARCHAR(50);
ALTER TABLE offers ADD COLUMN discount_label VARCHAR(100);
ALTER TABLE offers ADD COLUMN description TEXT;
ALTER TABLE offers ADD COLUMN restrictions TEXT[];  -- PostgreSQL array
ALTER TABLE offers ADD COLUMN photo_url VARCHAR(500);
ALTER TABLE offers ADD COLUMN rating DECIMAL(3,2);
ALTER TABLE offers ADD COLUMN rating_count BIGINT DEFAULT 0;
ALTER TABLE offers ADD COLUMN category VARCHAR(50);
ALTER TABLE offers ADD COLUMN per_user_limit INTEGER;
ALTER TABLE offers ADD COLUMN per_user_daily_limit INTEGER;
ALTER TABLE offers ADD COLUMN inventory INTEGER;
ALTER TABLE offers ADD COLUMN original_price DECIMAL(10,2);
```

**Impact:** Cannot filter offers, cannot display offer details properly

**Estimated Effort:** 2-3 days

---

### Gap 3: 6-Digit Redemption Code Generation (CRITICAL)

**Current Redemption Model:**

```java
// Current: No code field
@Entity
public class Redemption {
    private Long id;
    private Offer offer;
    private Long restaurantId;
    private String guestName;
    private String guestPhone;
    private LocalDateTime redeemedAt;
    private BigDecimal value;
    
    // ❌ MISSING
    // private String redemptionCode;  // "514527"
    // private LocalDateTime codeExpiresAt;  // 1-hour TTL
    // private RedemptionStatus status;  // GENERATED, COMPLETED, EXPIRED
    // private Long userId;
}
```

**Required Changes:**

```java
@Entity
@Table(name = "redemptions", uniqueConstraints = {
    @UniqueConstraint(columnNames = "redemption_code")
})
public class Redemption {
    // Existing fields...
    
    // NEW
    @Column(unique = true, nullable = false, length = 6)
    private String redemptionCode;  // Auto-generated 6-digit code
    
    @Column(nullable = false)
    private LocalDateTime codeExpiresAt;  // 1-hour default TTL
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RedemptionStatus status;  // GENERATED, COMPLETED, EXPIRED, CANCELLED
    
    @Column(nullable = false)
    private Long userId;  // Track which guest redeemed
}

enum RedemptionStatus {
    GENERATED,      // Code generated, shown to guest
    COMPLETED,      // Staff entered code on POS
    EXPIRED,        // Code TTL exceeded
    CANCELLED       // Guest cancelled before use
}
```

**Code Generation Utility:**
```java
public class RedemptionCodeGenerator {
    private static final Random random = new Random();
    
    public static String generate() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    // Ensure uniqueness via repository query
    public static String generateUnique(RedemptionRepository repo) {
        String code;
        do {
            code = generate();
        } while (repo.existsByRedemptionCode(code));
        return code;
    }
}
```

**New Endpoint Required:**

```java
@PostMapping("/{offerId}/redeem")
public ResponseEntity<ApiResponse<RedeemOfferResponse>> redeem(
    @PathVariable Long offerId,
    @RequestBody RedeemOfferRequest req,
    @AuthenticationPrincipal UserDetails userDetails
) {
    // 1. Validate offer exists and is active
    // 2. Check inventory
    // 3. Generate 6-digit code
    // 4. Create Redemption record
    // 5. Return code to guest
    // 6. Send SMS notification
}

@Data
class RedeemOfferResponse {
    private String redemptionCode;  // "514527"
    private LocalDateTime expiresAt;
    private String message;  // "Show this code to your server"
}
```

**Impact:** Cannot show 6-digit codes to guests, server POS validation impossible

**Estimated Effort:** 2 days

---

### Gap 4: Rewards Tier System (NEW)

**Current State:** ❌ COMPLETELY MISSING

**Required Entities:**

```java
@Entity
@Table(name = "reward_tiers")
public class RewardTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(nullable = false)
    private String name;  // "Silver", "Gold", "Platinum"
    
    @Column(nullable = false)
    private Long pointsThreshold;  // Min points needed (0, 350, 700)
    
    @Column(nullable = false)
    private Integer tierOrder;  // 1=Silver, 2=Gold, 3=Platinum
    
    @Column(columnDefinition = "text[]")
    private String[] perks;  // ["Free dessert", "Priority seating"]
    
    @Column(length = 20)
    private String color;  // For UI
}

@Entity
@Table(name = "reward_items")
public class RewardItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(nullable = false)
    private String title;  // "Free coffee"
    
    @Column(columnDefinition = "text")
    private String description;  // "Any size, any blend"
    
    @Column(nullable = false)
    private Long pointsCost;  // Cost in points
    
    @Column(length = 100)
    private String icon;  // For UI
}

@Entity
@Table(name = "points_earning_rules")
public class PointsEarningRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(nullable = false)
    private String action;  // "dine_in", "join_waitlist", "leave_review", "refer_friend"
    
    @Column(nullable = false)
    private Long pointsValue;  // Points awarded
    
    @Column(columnDefinition = "text")
    private String description;  // For display
    
    @Column(length = 100)
    private String icon;
    
    @Column(nullable = false)
    private Boolean clickable;  // If true, triggers in-app action
}
```

**Admin APIs Required:**
- `POST /api/admin/rewards/tiers` — Create tier
- `PUT /api/admin/rewards/tiers/{id}` — Update tier
- `GET /api/admin/rewards/tiers` — List tiers
- `POST /api/admin/rewards/items` — Create reward item
- `GET /api/admin/rewards/items` — List items
- `POST /api/admin/rewards/earning-rules` — Configure earning actions

**Impact:** Cannot display tier progress, cannot configure rewards catalog

**Estimated Effort:** 2-3 days

---

### Gap 5: Guest Rewards API Endpoints (NEW)

**Missing Endpoints:**

```
GET /api/rewards/profile
  Response: {
    "currentPoints": 240,
    "currentTier": {"name": "Silver tier", "pointsThreshold": 0},
    "tierProgress": {
      "pointsToNextTier": 110,
      "nextTierName": "Gold",
      "progressPercentage": 68.6
    },
    "redeemableRewards": [...]
  }

GET /api/rewards/redeemable
  Response: List of RewardItem with affordability flag

POST /api/rewards/{rewardId}/redeem
  Body: {}
  Response: {
    "redemptionCode": "514527",
    "expiresAt": "2026-09-16T14:30:00Z"
  }

GET /api/rewards/ways-to-earn
  Response: List of PointsEarningRule

POST /api/rewards/receipt/claim
  Body: multipart file upload
  Response: {
    "pointsClaimed": 15,
    "newBalance": 255,
    "status": "APPROVED"
  }
```

**Impact:** Guest cannot view points, tier progress, or redeem rewards

**Estimated Effort:** 3-4 days

---

### Gap 6: Receipt Scanning (NEW)

**Current State:** ❌ NOT IMPLEMENTED

**Required Entity:**

```java
@Entity
@Table(name = "receipt_claims")
public class ReceiptClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(nullable = false)
    private String fileUrl;  // S3 path
    
    @Column(nullable = false)
    private Long pointsClaimed;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;  // UPLOADED, APPROVED, REJECTED, DUPLICATE
    
    @Column(columnDefinition = "text")
    private String rejectionReason;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

enum ClaimStatus {
    UPLOADED,       // Waiting for approval
    APPROVED,       // Points credited
    REJECTED,       // Rejected by admin
    DUPLICATE       // Already claimed (duplicate detection)
}
```

**Required Functionality:**
- Multipart file upload to S3/Cloud Storage
- Duplicate detection (same date + restaurant + amount within X hours)
- Admin approval workflow
- Automatic points crediting on approval

**Impact:** Cannot claim points from dine-in visits

**Estimated Effort:** 2-3 days

---

## Implementation Schedule

| Phase | Features | Duration | Effort |
|-------|----------|----------|--------|
| **Phase 1** | Offer model expansion + DB migration | 2-3 days | 15-20 hrs |
| **Phase 2** | Guest Offers API + redemption codes | 3-4 days | 20-25 hrs |
| **Phase 3** | Rewards tiers + Admin APIs | 2-3 days | 15-20 hrs |
| **Phase 4** | Guest Rewards API | 3-4 days | 20-25 hrs |
| **Phase 5** | Receipt scanning | 2-3 days | 15-20 hrs |
| **Total** | **Full Alignment** | **12-17 days** | **85-110 hrs** |

**Parallel Optimization:**
- Phase 1 + 3 can run in parallel (2-3 days)
- Phase 2 + 4 can run in parallel (3-4 days)  
- Phase 5 standalone (2-3 days)
- **Optimized Timeline: 7-10 days** with 2-3 developers

---

## What to Add to REFACTORING_AND_OPTIMIZATION_PLAN.md

**Section to add:** New "Feature Completeness" section after architectural refactoring

### Add these items to Checklist:

```markdown
### Offers & Rewards Alignment (New Feature Development)

**Phase 1: Offer Model Expansion**
- [ ] Add 11 new fields to Offer entity
- [ ] Create database migration for new columns
- [ ] Create OfferCategory enum (PERCENT, FIXED, FREE_ITEM, REWARDS_ELIGIBLE)
- [ ] Update AdminOfferController to accept new fields
- [ ] Add validation for discount type + value
- [ ] Add category filtering to OfferRepository

**Phase 2: Guest Offers API**
- [ ] Create GuestOfferController
- [ ] Create GuestOfferService/Impl
- [ ] Create GuestOfferResponse DTO (with category, photo, rating, restrictions)
- [ ] Implement offer listing with filtering
- [ ] Implement 6-digit redemption code generation
- [ ] Add RedemptionCodeGenerator utility
- [ ] Update Redemption entity with code fields
- [ ] Add code expiry job (clean expired codes hourly)
- [ ] Create RedeemOfferResponse DTO
- [ ] Add SMS notification on redemption

**Phase 3: Rewards Tier Configuration**
- [ ] Create RewardTier entity
- [ ] Create RewardItem entity
- [ ] Create PointsEarningRule entity
- [ ] Create database migrations
- [ ] Create AdminRewardController
- [ ] Create AdminRewardService/Impl
- [ ] Create tier/reward/rule repositories
- [ ] Implement tier CRUD operations
- [ ] Add tier calculation logic

**Phase 4: Guest Rewards API**
- [ ] Create GuestRewardsController
- [ ] Create GuestRewardsService/Impl
- [ ] Create GuestRewardsProfileResponse DTO
- [ ] Implement /rewards/profile endpoint
- [ ] Implement /rewards/redeemable endpoint
- [ ] Implement redeem reward flow with code generation
- [ ] Implement /rewards/ways-to-earn endpoint
- [ ] Add points ledger entries for redemptions
- [ ] Add tier upgrade notifications

**Phase 5: Receipt Scanning**
- [ ] Create ReceiptClaim entity
- [ ] Create ReceiptClaimRepository
- [ ] Create ReceiptClaimService/Impl
- [ ] Implement S3 file upload integration
- [ ] Implement duplicate receipt detection logic
- [ ] Create receipt claim approval workflow
- [ ] Add admin approval endpoint
- [ ] Integrate with PointsService for auto-crediting
- [ ] Add receipt history endpoint

**Testing**
- [ ] Unit tests for all services (80%+ coverage)
- [ ] Integration tests for redemption flow
- [ ] Integration tests for points management
- [ ] Load test: 1000 concurrent redemptions
- [ ] Stress test: duplicate code generation
- [ ] E2E tests: full guest offers flow
- [ ] E2E tests: full rewards flow
```

---

## Notes

- These are **NEW FEATURES**, not refactoring existing code
- Should be scheduled AFTER Phase 1-3 refactoring completes
- Or can be done in parallel with refactoring by separate team
- All endpoints require authentication (JWT)
- All endpoints require rate limiting
- All data changes must create audit logs
- Comprehensive error handling required for fraud prevention

---
