# 🔐 Role & Permission System - Complete Implementation Guide

## Overview

This guide implements a **multi-tier, multi-tenant role-based access control system** where:
- **OWNER** manages their specific restaurant only
- **ADMIN** manages the entire system across all restaurants
- **MANAGER, HOST, STAFF** work within their assigned restaurant
- **GUEST** has public access only

---

## 📊 Architecture Overview

### Role Hierarchy
```
ADMIN (Level 5)           ← System Administrator (Platform staff)
  └─ Can manage: ALL restaurants, ALL users, system settings

OWNER (Level 4)           ← Restaurant Owner
  ├─ Can manage: Their restaurant only
  ├─ Can invite staff to their restaurant
  └─ Cannot see other restaurants

MANAGER (Level 3)         ← Operations Manager
  ├─ Can manage: Waitlist, table status, analytics
  └─ Cannot invite staff or change settings

HOST (Level 2)            ← Front Desk
  ├─ Can manage: Waitlist, guest interactions
  └─ Limited operations only

STAFF (Level 1)           ← General Staff
  └─ Can perform: Basic waitlist operations

GUEST (Level 0)           ← Customer
  └─ Public access: Join waitlist, view menu, submit feedback
```

### Database Identifier Strategy
```
OWNER/MANAGER/HOST/STAFF  → restaurantId = their restaurant
ADMIN                     → restaurantId = NULL (can access all)
GUEST                     → no restaurantId (no staff access)
```

---

## 1️⃣ DATABASE CHANGES

### 1.1 Update users Table

```sql
-- Add new columns
ALTER TABLE users ADD COLUMN staff_id BIGINT UNIQUE DEFAULT NULL;
ALTER TABLE users ADD COLUMN restaurant_id BIGINT DEFAULT NULL;

-- Add foreign keys
ALTER TABLE users ADD CONSTRAINT fk_users_staff_id 
  FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE SET NULL;

ALTER TABLE users ADD CONSTRAINT fk_users_restaurant_id 
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL;

-- Update role enum to support new roles
ALTER TABLE users MODIFY COLUMN role ENUM(
  'GUEST', 
  'ADMIN', 
  'OWNER', 
  'MANAGER', 
  'HOST', 
  'STAFF'
) NOT NULL DEFAULT 'GUEST';

-- Migrate existing RESTAURANT users to OWNER
UPDATE users SET role = 'OWNER' WHERE role = 'RESTAURANT';
```

### 1.2 Update staff Table

```sql
-- Add user_id link for authentication
ALTER TABLE staff ADD COLUMN user_id BIGINT UNIQUE DEFAULT NULL;
ALTER TABLE staff ADD CONSTRAINT fk_staff_user_id 
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Ensure required fields
ALTER TABLE staff MODIFY COLUMN email VARCHAR(255) UNIQUE NOT NULL;
ALTER TABLE staff MODIFY COLUMN role VARCHAR(50) NOT NULL;
ALTER TABLE staff MODIFY COLUMN password VARCHAR(255) DEFAULT NULL;
```

### 1.3 Users Table Final Schema

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  
  -- Role-based access
  role ENUM('GUEST', 'ADMIN', 'OWNER', 'MANAGER', 'HOST', 'STAFF') NOT NULL DEFAULT 'GUEST',
  
  -- Restaurant assignment (multi-tenant)
  restaurant_id BIGINT,            -- Assigned restaurant (NULL for ADMIN/GUEST)
  staff_id BIGINT UNIQUE,          -- Link to staff record
  
  -- Account status
  email_verified BOOLEAN DEFAULT FALSE,
  enabled BOOLEAN DEFAULT TRUE,
  
  -- Soft delete
  deleted_at TIMESTAMP NULL,
  
  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  -- Constraints
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL,
  FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE SET NULL,
  KEY idx_role (role),
  KEY idx_restaurant_id (restaurant_id)
);
```

### 1.4 Staff Table Final Schema

```sql
CREATE TABLE staff (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  restaurant_id BIGINT NOT NULL,
  
  -- Staff info
  name VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,              -- OWNER, MANAGER, HOST, STAFF
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255),
  
  -- Link to users table for authentication
  user_id BIGINT UNIQUE,
  
  -- Status
  status ENUM('ACTIVE', 'INVITED', 'INACTIVE') DEFAULT 'ACTIVE',
  
  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  -- Constraints
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  KEY idx_restaurant_id (restaurant_id),
  KEY idx_email (email)
);
```

---

## 2️⃣ ENTITY UPDATES

### 2.1 Update User.java

```java
package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP, enabled = false WHERE id = ?")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;

    // ✅ NEW: Restaurant assignment (for multi-tenant support)
    @Column(name = "restaurant_id")
    private Long restaurantId;

    // ✅ NEW: Link to staff record (if user is staff member)
    @Column(name = "staff_id")
    private Long staffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ✅ UPDATED: Extended role enum
    public enum UserRole {
        GUEST,      // Customer - no staff access
        STAFF,      // Restaurant general staff
        HOST,       // Restaurant front desk
        MANAGER,    // Restaurant operations
        OWNER,      // Restaurant owner
        ADMIN       // System administrator
    }
}
```

### 2.2 Update Staff.java

```java
package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "staff")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;  // OWNER, MANAGER, HOST, STAFF

    @Column(nullable = false)
    private String email;

    @Column(nullable = true, length = 255)
    private String password;

    // ✅ NEW: Link to User record for authentication
    @Column(name = "user_id", unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private StaffStatus status = StaffStatus.ACTIVE;

    public enum StaffStatus {
        ACTIVE, INVITED, INACTIVE
    }

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

## 3️⃣ SERVICE LAYER UPDATES

### 3.1 Create UserRepository Method

```java
// Add to UserRepository.java
package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    // ✅ NEW: Find by staff_id
    Optional<User> findByStaffId(Long staffId);
    
    // ✅ NEW: Find by restaurant and role
    List<User> findByRestaurantIdAndRole(Long restaurantId, User.UserRole role);
}
```

### 3.2 Update AdminStaffServiceImpl - setStaffPassword()

Replace the `setStaffPassword` method with this updated version that creates a User record:

```java
/**
 * Set password for staff after accepting invitation
 * Creates corresponding User record for authentication
 */
@Override
@Transactional
public AdminStaffResponse setStaffPassword(StaffSetPasswordRequest request) {
    // ... existing validation code ...
    
    StaffInvitationToken invitationToken = staffInvitationTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

    if (invitationToken.getIsUsed()) {
        throw new RuntimeException("This invitation has already been used");
    }

    if (!invitationToken.isValid()) {
        throw new RuntimeException("This invitation token has expired");
    }

    if (!request.getPassword().equals(request.getConfirmPassword())) {
        throw new RuntimeException("Passwords do not match");
    }

    if (request.getPassword().length() < 8) {
        throw new RuntimeException("Password must be at least 8 characters long");
    }

    Staff staff = invitationToken.getStaff();
    
    // Encrypt password
    String encryptedPassword = passwordEncoder.encode(request.getPassword());
    staff.setPassword(encryptedPassword);
    staff.setStatus(Staff.StaffStatus.ACTIVE);
    
    Staff savedStaff = staffRepository.save(staff);

    // ✅ NEW: Create User record for authentication
    User user = User.builder()
            .email(staff.getEmail())
            .password(encryptedPassword)
            .name(staff.getName())
            .phone(null)
            .role(User.UserRole.valueOf(staff.getRole().toUpperCase()))  // Map staff role to user role
            .restaurantId(staff.getRestaurant().getId())
            .staffId(staff.getId())
            .emailVerified(true)  // Already verified via invitation
            .enabled(true)
            .build();
    
    User savedUser = userRepository.save(user);
    
    // ✅ Link User back to Staff
    savedStaff.setUserId(savedUser.getId());
    staffRepository.save(savedStaff);

    // Mark token as used
    invitationToken.setIsUsed(true);
    staffInvitationTokenRepository.save(invitationToken);

    // Audit log
    AuditLog log = AuditLog.builder()
            .restaurantId(staff.getRestaurant().getId())
            .action("STAFF_ACTIVATED_VIA_INVITATION")
            .details("Staff " + staff.getName() + " (" + staff.getEmail() + ") activated with role: " + staff.getRole())
            .build();
    auditLogRepository.save(log);

    return AdminStaffResponse.builder()
            .id(savedStaff.getId())
            .name(savedStaff.getName())
            .role(savedStaff.getRole())
            .email(savedStaff.getEmail())
            .status(savedStaff.getStatus().name())
            .locationId(savedStaff.getRestaurant().getId())
            .location(savedStaff.getRestaurant().getName())
            .build();
}
```

### 3.3 Create RestaurantAccessService

```java
package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service for checking restaurant access permissions
 * Used in @PreAuthorize expressions to verify multi-tenant access
 */
@Service
@RequiredArgsConstructor
public class RestaurantAccessService {

    private final UserRepository userRepository;

    /**
     * Check if user can access a specific restaurant
     * 
     * ADMIN can access all restaurants
     * OWNER/MANAGER/HOST/STAFF can only access their assigned restaurant
     * GUEST cannot access any restaurant
     */
    public boolean canAccess(Long restaurantId, UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            return false;
        }
        
        // ADMIN can access any restaurant
        if (user.getRole() == User.UserRole.ADMIN) {
            return true;
        }
        
        // GUEST cannot access restaurants
        if (user.getRole() == User.UserRole.GUEST) {
            return false;
        }
        
        // Other roles can only access their assigned restaurant
        return user.getRestaurantId() != null && 
               user.getRestaurantId().equals(restaurantId);
    }

    /**
     * Check if user is OWNER of a specific restaurant
     * 
     * ADMIN is considered owner of all restaurants
     * OWNER role with matching restaurantId is owner of that restaurant
     * Others cannot own restaurants
     */
    public boolean isOwnerOf(Long restaurantId, UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            return false;
        }
        
        // ADMIN is considered owner of all restaurants
        if (user.getRole() == User.UserRole.ADMIN) {
            return true;
        }
        
        // Only OWNER role with matching restaurantId
        return user.getRole() == User.UserRole.OWNER && 
               user.getRestaurantId() != null && 
               user.getRestaurantId().equals(restaurantId);
    }

    /**
     * Check if user is MANAGER or OWNER of a restaurant
     */
    public boolean isManagerOrOwner(Long restaurantId, UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            return false;
        }
        
        if (user.getRole() == User.UserRole.ADMIN) {
            return true;
        }
        
        if (user.getRole() != User.UserRole.MANAGER && 
            user.getRole() != User.UserRole.OWNER) {
            return false;
        }
        
        return user.getRestaurantId() != null && 
               user.getRestaurantId().equals(restaurantId);
    }

    /**
     * Check if user has at least the specified role level
     */
    public boolean hasRoleLevel(User.UserRole requiredRole, User.UserRole userRole) {
        return getRoleLevel(userRole) >= getRoleLevel(requiredRole);
    }

    /**
     * Get numeric role level for comparison
     */
    private int getRoleLevel(User.UserRole role) {
        switch (role) {
            case ADMIN:
                return 5;
            case OWNER:
                return 4;
            case MANAGER:
                return 3;
            case HOST:
                return 2;
            case STAFF:
                return 1;
            case GUEST:
            default:
                return 0;
        }
    }
}
```

---

## 4️⃣ SECURITY CONFIGURATION

### 4.1 Update SecurityConfig.java

```java
package com.restaurant.waitlist.backend.config;

import com.restaurant.waitlist.backend.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // ✅ Enable @PreAuthorize annotations
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/waitlist/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/twilio/**").permitAll()
                        
                        // Guest endpoints
                        .requestMatchers("/api/menu/**").hasAnyRole("GUEST", "ADMIN")
                        .requestMatchers("/api/feedback/**").hasAnyRole("GUEST", "ADMIN")
                        
                        // ✅ Restaurant staff endpoints (require restaurant role)
                        .requestMatchers("/api/restaurants/**")
                            .hasAnyRole("STAFF", "HOST", "MANAGER", "OWNER", "ADMIN")
                        
                        .requestMatchers("/api/tables/**")
                            .hasAnyRole("HOST", "MANAGER", "OWNER", "ADMIN")
                        
                        // ✅ Staff management (OWNER can manage their restaurant, ADMIN manages all)
                        .requestMatchers("/api/admin/staff/**")
                            .hasAnyRole("OWNER", "ADMIN")
                        
                        // ✅ System admin endpoints (ADMIN only)
                        .requestMatchers("/api/admin/locations/**", "/api/admin/users/**", "/api/admin/system/**")
                            .hasRole("ADMIN")
                        
                        // ✅ All other admin endpoints (ADMIN only)
                        .requestMatchers("/api/admin/**")
                            .hasRole("ADMIN")
                        
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## 5️⃣ CONTROLLER AUTHORIZATION EXAMPLES

### 5.1 WaitlistController

```java
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/waitlist")
public class WaitlistController {
    
    /**
     * Any staff member can view their restaurant's waitlist
     * Verifies: user has restaurant role AND user is assigned to this restaurant
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'HOST', 'MANAGER', 'OWNER', 'ADMIN') and " +
                  "@restaurantAccessService.canAccess(#restaurantId, authentication.principal)")
    public ResponseEntity<?> getWaitlist(@PathVariable Long restaurantId) {
        // Can be STAFF, HOST, MANAGER, OWNER, or ADMIN
        // Must be assigned to this restaurant (except ADMIN who can see all)
    }
    
    /**
     * Only HOST, MANAGER, OWNER can modify waitlist
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HOST', 'MANAGER', 'OWNER', 'ADMIN') and " +
                  "@restaurantAccessService.canAccess(#restaurantId, authentication.principal)")
    public ResponseEntity<?> addToWaitlist(@PathVariable Long restaurantId, @RequestBody JoinWaitlistRequest request) {
        // Only HOST and above can add to waitlist
    }
}
```

### 5.2 RestaurantController

```java
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    
    /**
     * OWNER can manage their restaurant
     * ADMIN can manage any restaurant
     * Verifies: owner of this restaurant
     */
    @PostMapping("/{restaurantId}/staff")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') and " +
                  "@restaurantAccessService.isOwnerOf(#restaurantId, authentication.principal)")
    public ResponseEntity<?> inviteStaff(
            @PathVariable Long restaurantId,
            @RequestBody AdminStaffRequest request) {
        // Only OWNER (of their restaurant) or ADMIN (any restaurant)
    }
    
    /**
     * Only OWNER can change settings
     */
    @PutMapping("/{restaurantId}/settings")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') and " +
                  "@restaurantAccessService.isOwnerOf(#restaurantId, authentication.principal)")
    public ResponseEntity<?> updateSettings(
            @PathVariable Long restaurantId,
            @RequestBody SettingsRequest request) {
        // Only OWNER of this restaurant can change settings
    }
}
```

### 5.3 AdminStaffController

```java
@RestController
@RequestMapping("/api/admin/staff")
public class AdminStaffController {
    
    /**
     * OWNER can invite staff to their restaurant
     * ADMIN can invite staff to any restaurant
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> invite(@Valid @RequestBody AdminStaffRequest request) {
        // OWNER: verify they own the locationId restaurant
        // ADMIN: can use any locationId
        
        Long locationId = request.getLocationId();
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal.getRole() == User.UserRole.OWNER) {
            // OWNER: verify access
            if (!restaurantAccessService.isOwnerOf(locationId, SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
                throw new AccessDeniedException("You can only invite staff to your own restaurant");
            }
        }
        
        // Proceed with invitation
    }
}
```

### 5.4 AdminSystemController (ADMIN ONLY)

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // ✅ All endpoints require ADMIN role
public class AdminSystemController {
    
    /**
     * Only ADMIN can view all restaurants across system
     */
    @GetMapping("/restaurants")
    public ResponseEntity<?> getAllRestaurants(Pageable pageable) {
        // ADMIN can see all restaurants
    }
    
    /**
     * Only ADMIN can create new restaurants
     */
    @PostMapping("/restaurants")
    public ResponseEntity<?> createRestaurant(@RequestBody RestaurantRequest request) {
        // ADMIN can create restaurants
    }
    
    /**
     * Only ADMIN can view all users across system
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Pageable pageable) {
        // ADMIN can see all users
    }
    
    /**
     * Only ADMIN can manage system settings
     */
    @PutMapping("/system/settings")
    public ResponseEntity<?> updateSystemSettings(@RequestBody SystemSettingsRequest request) {
        // ADMIN can change system-wide settings
    }
}
```

---

## 6️⃣ ROLE PERMISSION MATRIX

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          PERMISSION MATRIX                              │
├─────────────────────────────────────────────────────────────────────────┤
│ Feature                    │ GUEST │ STAFF │ HOST │ MGR │ OWNER │ ADMIN │
├─────────────────────────────────────────────────────────────────────────┤
│ Join Waitlist              │  ✓    │   ✗   │  ✗   │  ✗  │   ✗   │   ✗   │
│ View Waitlist              │  ✗    │   ✓   │  ✓   │  ✓  │   ✓   │   ✓   │
│ Manage Waitlist            │  ✗    │   ✓   │  ✓   │  ✓  │   ✓   │   ✓   │
│ Seat Guests                │  ✗    │   ✗   │  ✓   │  ✓  │   ✓   │   ✓   │
│ Notify Guests              │  ✗    │   ✓   │  ✓   │  ✓  │   ✓   │   ✓   │
│ View Tables                │  ✗    │   ✗   │  ✓   │  ✓  │   ✓   │   ✓   │
│ Manage Tables              │  ✗    │   ✗   │  ✗   │  ✓  │   ✓   │   ✓   │
│ View Analytics             │  ✗    │   ✗   │  ✓   │  ✓  │   ✓   │   ✓   │
│ Invite Staff (Own)         │  ✗    │   ✗   │  ✗   │  ✗  │   ✓   │   ✓   │
│ Manage Staff (Own)         │  ✗    │   ✗   │  ✗   │  ✗  │   ✓   │   ✓   │
│ Update Settings (Own)      │  ✗    │   ✗   │  ✗   │  ✗  │   ✓   │   ✓   │
│ View Own Restaurant        │  ✗    │   ✓   │  ✓   │  ✓  │   ✓   │   ✓   │
│ View Other Restaurants     │  ✗    │   ✗   │  ✗   │  ✗  │   ✗   │   ✓   │
│ Create Restaurant          │  ✗    │   ✗   │  ✗   │  ✗  │   ✗   │   ✓   │
│ Manage All Users           │  ✗    │   ✗   │  ✗   │  ✗  │   ✗   │   ✓   │
│ System Settings            │  ✗    │   ✗   │  ✗   │  ✗  │   ✗   │   ✓   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 7️⃣ USER FLOW EXAMPLES

### Flow 1: New OWNER Registration

```
1. Frontend: User registers at /api/auth/register-owner
   POST /api/auth/register-owner
   {
     "email": "owner@restaurant1.com",
     "password": "SecurePassword123",
     "restaurantName": "My Restaurant",
     "name": "John Owner"
   }

2. Backend: AuthService.registerOwner()
   - Creates Restaurant record
   - Creates User with:
     • role = OWNER
     • restaurantId = 1 (their restaurant)
     • staffId = NULL

3. User verifies email
   - POST /api/auth/verify-email?token=xxx

4. User logs in
   - POST /api/auth/login
   - Returns: JWT with role=OWNER, restaurantId=1

5. User can:
   - View their restaurant's waitlist ✓
   - Invite staff to their restaurant ✓
   - Change their restaurant's settings ✓
   - Cannot view other restaurants ✗
   - Cannot create new restaurants ✗
```

### Flow 2: New STAFF Invitation

```
1. OWNER invites staff
   POST /api/restaurants/1/staff (with OWNER token)
   {
     "name": "Jane Host",
     "role": "HOST",
     "email": "jane@example.com",
     "locationId": 1
   }

2. Backend: AdminStaffService.inviteStaff()
   - Creates Staff record with status=INVITED
   - Generates invitation token (24hr valid)
   - Sends email with link: /staff/accept-invitation?token=xxx

3. Staff clicks link and verifies token
   - POST /api/admin/staff/verify-invitation
   - { "token": "xxx" }

4. Staff sets password
   - POST /api/admin/staff/set-password
   - { "token": "xxx", "password": "xxx", "confirmPassword": "xxx" }

5. Backend: AdminStaffService.setStaffPassword()
   - Updates Staff.password (encrypted)
   - Updates Staff.status = ACTIVE
   - ✅ Creates User record with:
     • email = staff.email
     • password = encrypted password
     • role = HOST (from staff.role)
     • restaurantId = 1
     • staffId = staff.id

6. Staff logs in
   - POST /api/auth/login
   - Returns: JWT with role=HOST, restaurantId=1

7. Staff can:
   - View their restaurant's waitlist ✓
   - Manage waitlist for their restaurant ✓
   - Seat guests ✓
   - Cannot invite other staff ✗
   - Cannot change settings ✗
```

### Flow 3: ADMIN System Access

```
1. ADMIN logs in (created via backend/migration)
   - POST /api/auth/login
   - Returns: JWT with role=ADMIN, restaurantId=NULL

2. ADMIN can:
   - Access /api/admin/restaurants → See ALL restaurants ✓
   - Access /api/admin/users → See ALL users ✓
   - Access /api/admin/staff → Manage staff at ANY restaurant ✓
   - Access /api/admin/system/settings → Change system config ✓
   - Post to /api/admin/restaurants → Create new restaurant ✓
   - Access any restaurant's data (restaurantId ignored) ✓
```

---

## 8️⃣ JWT TOKEN STRUCTURE

### Token Payload (Enhanced)

```java
// JwtTokenProvider.generateToken(User user)
{
  "sub": "user@example.com",
  "id": 123,
  "email": "user@example.com",
  "role": "OWNER",                    // ✅ Role included
  "restaurantId": 1,                  // ✅ Restaurant assignment
  "staffId": null,                    // ✅ Staff link
  "iat": 1676543210,
  "exp": 1676629610
}
```

### JwtFilter Usage

```java
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        
        String token = extractTokenFromRequest(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Claims claims = jwtTokenProvider.getClaims(token);
            
            String email = (String) claims.get("email");
            String role = (String) claims.get("role");
            Long restaurantId = claims.get("restaurantId") != null ? 
                                ((Number) claims.get("restaurantId")).longValue() : null;
            
            // Create security context with role authority
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(email, null, Collections.singleton(authority));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## 9️⃣ MIGRATION SCRIPT

```sql
-- Step 1: Add columns to users table
ALTER TABLE users ADD COLUMN staff_id BIGINT UNIQUE DEFAULT NULL;
ALTER TABLE users ADD COLUMN restaurant_id BIGINT DEFAULT NULL;

-- Step 2: Add columns to staff table
ALTER TABLE staff ADD COLUMN user_id BIGINT UNIQUE DEFAULT NULL;

-- Step 3: Update existing RESTAURANT users to OWNER
UPDATE users SET role = 'OWNER' WHERE role = 'RESTAURANT';

-- Step 4: Add foreign keys
ALTER TABLE users ADD CONSTRAINT fk_users_staff_id 
  FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE SET NULL;

ALTER TABLE users ADD CONSTRAINT fk_users_restaurant_id 
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL;

ALTER TABLE staff ADD CONSTRAINT fk_staff_user_id 
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Step 5: Migrate existing RESTAURANT users to OWNER
-- (Example: if RESTAURANT users have restaurant_id already set)
UPDATE users u 
SET u.restaurant_id = (SELECT id FROM restaurants LIMIT 1)
WHERE u.role = 'OWNER' AND u.restaurant_id IS NULL;

-- Step 6: Create ADMIN user (manually or via script)
INSERT INTO users (email, password, name, phone, role, restaurant_id, staff_id, email_verified, enabled, created_at, updated_at)
VALUES ('admin@dinerly.com', '$2a$10$...bcrypt_hash...', 'System Admin', NULL, 'ADMIN', NULL, NULL, true, true, NOW(), NOW());
```

---

## 🔟 TESTING SCENARIOS

### Test 1: OWNER accessing their restaurant

```bash
# 1. Login as OWNER
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@restaurant1.com",
    "password": "password123"
  }'

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "role": "OWNER",
    "restaurantId": 1
  }
}

# 2. Access their restaurant's waitlist (should work)
curl -X GET http://localhost:8080/api/restaurants/1/waitlist \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# ✅ SUCCESS 200

# 3. Try to access different restaurant (should fail)
curl -X GET http://localhost:8080/api/restaurants/2/waitlist \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# ✗ FORBIDDEN 403
```

### Test 2: STAFF with limited access

```bash
# 1. Login as STAFF/HOST
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "password": "password123"
  }'

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "role": "HOST",
    "restaurantId": 1
  }
}

# 2. Can view waitlist
curl -X GET http://localhost:8080/api/restaurants/1/waitlist \
  -H "Authorization: Bearer ..."

# ✅ SUCCESS 200

# 3. Cannot invite staff (not OWNER)
curl -X POST http://localhost:8080/api/restaurants/1/staff \
  -H "Authorization: Bearer ..." \
  -H "Content-Type: application/json" \
  -d '{"name": "John", "role": "STAFF", "email": "john@example.com"}'

# ✗ FORBIDDEN 403 - Missing authority: ROLE_OWNER or ROLE_ADMIN
```

### Test 3: ADMIN accessing all restaurants

```bash
# 1. Login as ADMIN
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@dinerly.com",
    "password": "password123"
  }'

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "role": "ADMIN",
    "restaurantId": null  // ← NULL for ADMIN
  }
}

# 2. Can access any restaurant
curl -X GET http://localhost:8080/api/restaurants/1/waitlist \
  -H "Authorization: Bearer ..."

# ✅ SUCCESS 200

# 3. Can access admin endpoints
curl -X GET http://localhost:8080/api/admin/restaurants \
  -H "Authorization: Bearer ..."

# ✅ SUCCESS 200 - Returns ALL restaurants

# 4. Can create new restaurant
curl -X POST http://localhost:8080/api/admin/restaurants \
  -H "Authorization: Bearer ..." \
  -d '{"name": "New Rest", ...}'

# ✅ SUCCESS 201
```

---

## 1️⃣1️⃣ CHECKLIST FOR IMPLEMENTATION

### Database
- [ ] Run database migration scripts
- [ ] Add staff_id and restaurant_id to users table
- [ ] Add user_id to staff table
- [ ] Update role enum with new values
- [ ] Migrate existing RESTAURANT users to OWNER
- [ ] Create ADMIN user

### Entities
- [ ] Update User.java with new fields and extended UserRole enum
- [ ] Update Staff.java with user_id field
- [ ] Compile and verify no errors

### Repositories
- [ ] Add UserRepository methods (findByStaffId, findByRestaurantId)
- [ ] Update StaffRepository with user_id queries

### Services
- [ ] Create RestaurantAccessService with access checks
- [ ] Update AdminStaffServiceImpl.setStaffPassword() to create User
- [ ] Add UserRepository injection to services

### Security
- [ ] Update SecurityConfig.java with new role-based rules
- [ ] Verify @EnableMethodSecurity is enabled
- [ ] Update JwtTokenProvider to include role and restaurantId in token

### Controllers
- [ ] Add @PreAuthorize annotations to protected endpoints
- [ ] Update AdminStaffController endpoints
- [ ] Add RestaurantAccessService checks where needed
- [ ] Update error handling for access denied

### Testing
- [ ] Test OWNER login and restaurant access
- [ ] Test STAFF limited access
- [ ] Test ADMIN system access
- [ ] Test cross-restaurant access denial
- [ ] Test staff invitation flow
- [ ] Test role permission matrix

### Documentation
- [ ] Update API documentation with role requirements
- [ ] Document @PreAuthorize expressions used
- [ ] Create migration guide for existing users

---

## 1️⃣2️⃣ SUMMARY

✅ **Implemented:**
- Multi-tier role hierarchy (GUEST, STAFF, HOST, MANAGER, OWNER, ADMIN)
- Multi-tenant support (restaurantId for scoping)
- User-Staff bidirectional linking
- @PreAuthorize security annotations
- RestaurantAccessService for authorization checks
- JWT token with role and restaurantId
- Complete staff invitation workflow
- Role-based permission matrix

✅ **Key Differences:**
- OWNER: Limited to their restaurant only
- ADMIN: Can access all restaurants and system settings
- STAFF/HOST/MANAGER: Limited to their assigned restaurant
- GUEST: No staff access

✅ **Database Strategy:**
- OWNER/STAFF/HOST/MANAGER → restaurantId = their restaurant
- ADMIN → restaurantId = NULL (can access all)
- GUEST → no restaurantId

**Ready for implementation!** Follow the checklist above to integrate with your codebase.

