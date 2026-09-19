# 🔐 ROLE & PERMISSION SYSTEM - IMPLEMENTATION STATUS

**Date:** September 19, 2026  
**Status:** ✅ PARTIALLY IMPLEMENTED  

---

## ✅ COMPLETED CHANGES

### 1. Entity Updates
- ✅ **User.java** - Added `staffId` field and extended UserRole enum (GUEST, STAFF, HOST, MANAGER, OWNER, ADMIN)
- ✅ **Staff.java** - Added `userId` field for linking to User table

### 2. Repository Updates
- ✅ **UserRepository.java** - Added 3 new query methods:
  - `findByStaffId(Long staffId)` - Find user by staff ID
  - `findByRestaurantIdAndRole()` - Find users by restaurant and role
  - `findAllRestaurantStaff()` - Find all active staff in restaurant

### 3. Security Configuration
- ✅ **SecurityConfig.java** - Updated authorization rules:
  - Restaurant staff endpoints → STAFF, HOST, MANAGER, OWNER, ADMIN roles
  - Table management → HOST, MANAGER, OWNER, ADMIN roles
  - Staff management → OWNER, ADMIN only
  - System admin endpoints → ADMIN only

### 4. Service Layer
- ✅ **AdminStaffServiceImpl.java** - Updated `setStaffPassword()` method to:
  - Create User record when staff accepts invitation
  - Link User to Staff bidirectionally
  - Set correct role mapping from staff.role
  - Set restaurantId correctly

---

## ⏳ STILL NEEDED

### 1. Create RestaurantAccessService
**File Location:** `/src/main/java/com/restaurant/waitlist/backend/service/RestaurantAccessService.java`

**Status:** ⏳ Can't create (timeout issue with tools)

**Purpose:** Provides authorization checks for @PreAuthorize expressions

**Key Methods:**
- `canAccess(restaurantId, userDetails)` - Check if user can access restaurant
- `isOwnerOf(restaurantId, userDetails)` - Check if user is owner
- `isManagerOrOwner(restaurantId, userDetails)` - Check if manager/owner

**Action:** Create manually using the code from ROLE_AND_PERMISSION_IMPLEMENTATION.md (Section 3.3)

### 2. Update Controllers with @PreAuthorize

**Files to Update:**
- `WaitlistController.java` - Add @PreAuthorize to endpoints
- `RestaurantController.java` - Add @PreAuthorize for owner-only operations  
- `AdminStaffController.java` - Restrict to OWNER + ADMIN
- `AdminSystemController.java` - Restrict to ADMIN only

**Example Annotations:**
```java
@PreAuthorize("hasAnyRole('HOST', 'MANAGER', 'OWNER', 'ADMIN') and " +
              "@restaurantAccessService.canAccess(#restaurantId, authentication.principal)")
```

### 3. Database Migration

**File:** `DATABASE_MIGRATION_ROLES.sql` (Created ✅)

**Status:** ⏳ Needs to be executed

**Steps:**
1. Backup your database
2. Run: `mysql -u root -p < DATABASE_MIGRATION_ROLES.sql`
3. Verify changes (see verification queries in the file)

### 4. JwtFilter Enhancement (Optional)

**Current:** JwtFilter already includes role in token  
**Enhancement:** Also include restaurantId in JWT claims

**Location:** `JwtTokenProvider.java` or `JwtFilter.java`

---

## ❌ KNOWN ISSUES & LIMITATIONS

1. **RestaurantAccessService** - Could not create due to tool timeout
   - **Solution:** Copy from ROLE_AND_PERMISSION_IMPLEMENTATION.md Section 3.3 and create manually

2. **Timestamp Issue** - Some audit logs show ID instead of timestamp
   - **Status:** Non-critical, can be fixed later

---

## 📋 STEP-BY-STEP COMPLETION GUIDE

### Phase 1: Database & Entities (✅ DONE)
```
✅ Updated User entity
✅ Updated Staff entity  
✅ Updated UserRepository
✅ Created migration script
```

### Phase 2: Security Configuration (✅ DONE)
```
✅ Updated SecurityConfig
✅ Verified @EnableMethodSecurity enabled
```

### Phase 3: Service Layer (✅ DONE)
```
✅ Updated AdminStaffServiceImpl.setStaffPassword()
✅ Now creates User record automatically
```

### Phase 4: Authorization Service (⏳ TODO)
```
⏳ Create RestaurantAccessService manually
   - Copy code from ROLE_AND_PERMISSION_IMPLEMENTATION.md
   - Save as RestaurantAccessService.java
   - Run Maven build to verify compilation
```

### Phase 5: Controller Security (⏳ TODO)
```
⏳ Add @PreAuthorize to endpoints
⏳ Test each role access level
⏳ Verify cross-restaurant access denial
```

### Phase 6: Database Migration (⏳ TODO)
```
⏳ Execute DATABASE_MIGRATION_ROLES.sql
⏳ Verify migration with provided SQL queries
⏳ Test existing data integrity
```

### Phase 7: Testing & Verification (⏳ TODO)
```
⏳ Test OWNER login and access
⏳ Test STAFF limited access
⏳ Test ADMIN full access
⏳ Test cross-restaurant denial
⏳ Test staff invitation flow
```

---

## 🔧 QUICK IMPLEMENTATION STEPS

### Step 1: Create RestaurantAccessService
```bash
cd src/main/java/com/restaurant/waitlist/backend/service/
# Create RestaurantAccessService.java
# Copy code from ROLE_AND_PERMISSION_IMPLEMENTATION.md section 3.3
```

### Step 2: Run Database Migration
```bash
mysql -u your_user -p < DATABASE_MIGRATION_ROLES.sql
```

### Step 3: Update WaitlistController
Add these annotations to endpoints:
```java
@PreAuthorize("hasAnyRole('STAFF', 'HOST', 'MANAGER', 'OWNER', 'ADMIN') and " +
              "@restaurantAccessService.canAccess(#restaurantId, authentication.principal)")
```

### Step 4: Build & Test
```bash
cd /Users/dineshkumar/Downloads/backend
./mvnw clean compile
./mvnw spring-boot:run
```

### Step 5: Test API Endpoints
See testing scenarios in ROLE_AND_PERMISSION_IMPLEMENTATION.md (Section 10)

---

## 📚 REFERENCE FILES

1. **ROLE_AND_PERMISSION_IMPLEMENTATION.md** - Complete implementation guide with all code examples
2. **DATABASE_MIGRATION_ROLES.sql** - Database migration script
3. Updated source files:
   - User.java
   - Staff.java
   - UserRepository.java
   - SecurityConfig.java
   - AdminStaffServiceImpl.java

---

## 🎯 KEY IMPLEMENTATION DETAILS

### Role Hierarchy
```
ADMIN (Level 5) - System administrator
  └─ Can manage: ALL restaurants, ALL users, system settings
  
OWNER (Level 4) - Restaurant owner
  └─ Can manage: Their restaurant only
  
MANAGER (Level 3) - Operations manager
  └─ Can manage: Waitlist, tables, analytics
  
HOST (Level 2) - Front desk
  └─ Can manage: Waitlist actions
  
STAFF (Level 1) - General staff
  └─ Can perform: Basic operations
  
GUEST (Level 0) - Customer
  └─ Can: Join waitlist, submit feedback
```

### Multi-Tenant Scoping
- **OWNER/MANAGER/HOST/STAFF** → restaurantId = their assigned restaurant
- **ADMIN** → restaurantId = NULL (can access all restaurants)
- **GUEST** → no restaurantId (no staff access)

### User Authentication Flow
1. **OWNER**: Direct registration → role='OWNER', restaurantId=their_restaurant
2. **STAFF**: Invited by OWNER → staff record created → password set → User record created with role=STAFF/HOST/MANAGER/OWNER

---

## ✅ VERIFICATION CHECKLIST

### Before Going Live
- [ ] RestaurantAccessService created and compiles
- [ ] Database migration executed
- [ ] RESTAURANT users migrated to OWNER
- [ ] @PreAuthorize annotations added to controllers
- [ ] Build completes without errors: `./mvnw clean compile`
- [ ] Application starts: `./mvnw spring-boot:run`

### Testing Checklist
- [ ] OWNER can access their restaurant
- [ ] OWNER cannot access other restaurants
- [ ] ADMIN can access all restaurants
- [ ] STAFF has limited access per role
- [ ] GUEST has no restaurant access
- [ ] Staff invitation creates User record
- [ ] JWT token includes role and restaurantId
- [ ] @PreAuthorize blocks unauthorized access

---

## 📞 COMMON ISSUES & SOLUTIONS

**Issue:** "Missing bean 'restaurantAccessService'"  
**Solution:** Ensure RestaurantAccessService.java exists and is in correct package

**Issue:** "Cannot convert RESTAURANT to OWNER"  
**Solution:** Migrate existing users: `UPDATE users SET role = 'OWNER' WHERE role = 'RESTAURANT';`

**Issue:** @PreAuthorize not working  
**Solution:** Verify `@EnableMethodSecurity` is in SecurityConfig

---

## 🚀 WHAT'S NEXT

After completing all these steps:

1. **Frontend Integration** - Update frontend to send correct role/restaurantId
2. **API Documentation** - Update Swagger docs with new role requirements
3. **User Management** - Add endpoints for admins to manage roles
4. **Audit Logging** - Track all role-based access decisions
5. **Testing Suite** - Add integration tests for authorization

---

Generated: September 19, 2026
Status: Ready for manual completion

