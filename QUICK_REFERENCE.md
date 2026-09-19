# 🔑 ROLE & PERMISSION SYSTEM - QUICK REFERENCE

## ✅ WHAT'S BEEN IMPLEMENTED

### Code Changes (6 Files Modified + 1 New File)

```
MODIFIED FILES:
✅ src/main/java/.../entity/User.java
   - Added: Long staffId
   - Updated: UserRole enum (GUEST, STAFF, HOST, MANAGER, OWNER, ADMIN)

✅ src/main/java/.../entity/Staff.java
   - Added: Long userId

✅ src/main/java/.../repository/UserRepository.java
   - Added: findByStaffId()
   - Added: findByRestaurantIdAndRole()
   - Added: findAllRestaurantStaff()

✅ src/main/java/.../config/SecurityConfig.java
   - Updated: @RequestMatchers for new roles
   - Changed: "RESTAURANT" → "STAFF", "HOST", "MANAGER", "OWNER", "ADMIN"

✅ src/main/java/.../service/admin/AdminStaffServiceImpl.java
   - Added: UserRepository injection
   - Enhanced: setStaffPassword() creates User record
   - Bonus: Bidirectional User ↔ Staff linking

✅ src/main/java/.../service/AuthService.java
   - Fixed: UserRole.RESTAURANT → UserRole.OWNER

NEW FILES:
✅ src/main/java/.../service/RestaurantAccessService.java
   - canAccess() - Multi-tenant access check
   - isOwnerOf() - Owner verification
   - isManagerOrOwner() - Manager/Owner check
   
DOCUMENTATION:
✅ DATABASE_MIGRATION_ROLES.sql - Ready to execute
✅ ROLE_AND_PERMISSION_IMPLEMENTATION.md - Complete guide
✅ IMPLEMENTATION_STATUS.md - Status tracking
✅ IMPLEMENTATION_COMPLETE.md - Final summary
✅ QUICK_REFERENCE.md - This file
```

---

## 🔄 USER FLOW EXAMPLES

### New OWNER Registration
```
User registers with role=OWNER → restaurantId=their_restaurant
↓
User sets password
↓
User logs in → JWT(role=OWNER, restaurantId=1)
↓
Can access: /api/restaurants/{1}/waitlist ✅
Cannot access: /api/restaurants/{2}/waitlist ❌
```

### STAFF Invitation
```
OWNER: POST /api/admin/staff
↓
System: Create Staff + send email with token
↓
Staff: Click link + verify token
↓
Staff: POST /api/admin/staff/set-password
↓
System: Auto-create User(role=HOST/MANAGER/STAFF/OWNER, restaurantId=1)
↓
Staff: Login with email+password → JWT
✅ Ready to work!
```

### ADMIN System Access
```
ADMIN logs in → JWT(role=ADMIN, restaurantId=NULL)
↓
Can access: /api/admin/restaurants → ALL restaurants ✅
Can access: /api/admin/users → ALL users ✅
Can access: /api/admin/system/settings → System config ✅
Can create: New restaurants ✅
```

---

## 🔒 AUTHORIZATION EXAMPLES

### Before Implementation
```java
@PreAuthorize("hasRole('RESTAURANT')")
public ResponseEntity<?> manageWaitlist() { }  // ❌ Old way
```

### After Implementation
```java
@PreAuthorize("hasAnyRole('STAFF', 'HOST', 'MANAGER', 'OWNER', 'ADMIN') and " +
              "@restaurantAccessService.canAccess(#restaurantId, authentication.principal)")
public ResponseEntity<?> getWaitlist(@PathVariable Long restaurantId) {
    // ✅ Requires correct role AND access to restaurant
}

@PreAuthorize("hasAnyRole('OWNER', 'ADMIN') and " +
              "@restaurantAccessService.isOwnerOf(#restaurantId, authentication.principal)")
public ResponseEntity<?> inviteStaff(@PathVariable Long restaurantId) {
    // ✅ Only OWNER of restaurant or ADMIN can invite staff
}

@PreAuthorize("hasRole('ADMIN')")  // ✅ ADMIN only
public ResponseEntity<?> getAllRestaurants() {
    // Can see and manage all restaurants
}
```

---

## ✅ COMPILATION STATUS

```
Last Build: September 19, 2026 16:17:43 IST

✅ All 295 source files compiled successfully
✅ 0 Compilation errors
✅ RestaurantAccessService.java compiles ✅
✅ AdminStaffServiceImpl.java (with User creation) ✅
✅ SecurityConfig.java (with new roles) ✅

BUILD: SUCCESS ✓
```

---

## ⏳ STILL TODO

### 1. Database Migration (5-10 minutes)
```bash
# In your database client or terminal:
mysql -u root -p < DATABASE_MIGRATION_ROLES.sql

# Verify with:
SELECT DISTINCT role FROM users;  # Should show all 6 new roles
DESCRIBE users;  # Should show staff_id and restaurant_id columns
```

### 2. Update Controllers (30-60 minutes)
Files needing @PreAuthorize annotations:
- [ ] WaitlistController.java
- [ ] RestaurantController.java
- [ ] AdminStaffController.java
- [ ] TableController.java
- [ ] SettingsController.java
- [ ] StaffController.java
- [ ] NotificationController.java
- [ ] UserController.java

Example changes:
```java
// OLD:
@PreAuthorize("hasRole('RESTAURANT')")

// NEW for staff endpoints:
@PreAuthorize("hasAnyRole('STAFF', 'HOST', 'MANAGER', 'OWNER', 'ADMIN') and " +
              "@restaurantAccessService.canAccess(#restaurantId, authentication.principal)")

// NEW for OWNER only:
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN') and " +
              "@restaurantAccessService.isOwnerOf(#restaurantId, authentication.principal)")
```

### 3. Testing (20-30 minutes)
```bash
./mvnw test

# Manual testing:
# 1. Login as OWNER → access own restaurant
# 2. Login as OWNER → try to access other restaurant (should fail)
# 3. Login as ADMIN → access all restaurants
# 4. Invite staff → verify User record auto-created
# 5. Login as invited staff with password
```

### 4. Deployment (15-20 minutes)
```bash
./mvnw clean package
java -jar target/backend-*.jar
```

---

## 🎯 ROLE ACCESS MATRIX

| Endpoint | GUEST | STAFF | HOST | MGR | OWNER | ADMIN |
|----------|-------|-------|------|-----|-------|-------|
| `/api/waitlist` (POST) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/api/restaurants/{id}/waitlist` | ✗ | ✅* | ✅* | ✅* | ✅* | ✅ |
| `/api/admin/staff` | ✗ | ✗ | ✗ | ✗ | ✅* | ✅ |
| `/api/admin/restaurants` | ✗ | ✗ | ✗ | ✗ | ✗ | ✅ |
| `/api/admin/users` | ✗ | ✗ | ✗ | ✗ | ✗ | ✅ |

✅ = Allowed  
✗ = Blocked  
✅* = Allowed only for own restaurant  

---

## 🔐 ROLE LEVEL SYSTEM

```
getRoleLevel(role):
  ADMIN   = 5  (highest)
  OWNER   = 4
  MANAGER = 3
  HOST    = 2
  STAFF   = 1
  GUEST   = 0  (lowest)

Usage:
  if (getRoleLevel(userRole) >= getRoleLevel(MANAGER)) {
    // Can do manager tasks
  }
```

---

## 📱 JWT TOKEN FORMAT

```json
{
  "sub": "user@example.com",
  "id": 123,
  "email": "user@example.com",
  "role": "OWNER",
  "restaurantId": 1,
  "staffId": null,
  "iat": 1695127863,
  "exp": 1695214263
}
```

Key fields:
- `role` - User role (OWNER, STAFF, etc.)
- `restaurantId` - Assigned restaurant (NULL for ADMIN)
- `staffId` - Staff ID if user has staff record

---

## 🗄️ DATABASE STRUCTURE

### users table (updated)
```sql
id               (PK)
email            (UNIQUE)
password         (BCrypt)
name
phone
role             (ENUM: 6 values)
restaurant_id    (FK: restaurants.id) ← NEW
staff_id         (FK: staff.id, UNIQUE) ← NEW
email_verified
enabled
deleted_at
created_at
updated_at
```

### staff table (updated)
```sql
id               (PK)
restaurant_id    (FK: restaurants.id)
name
role             (TEXT)
email
password
user_id          (FK: users.id, UNIQUE) ← NEW
status           (ENUM)
created_at
updated_at
```

---

## 🚀 NEXT STEPS COMMAND-LINE

```bash
# Step 1: Backup your database
mysqldump -u root -p your_database > backup_$(date +%Y%m%d).sql

# Step 2: Run migration
mysql -u root -p < DATABASE_MIGRATION_ROLES.sql

# Step 3: Verify migration
mysql -u root -p -e "SELECT DISTINCT role FROM your_database.users;"

# Step 4: Rebuild application
cd /Users/dineshkumar/Downloads/backend
./mvnw clean compile

# Step 5: Run tests
./mvnw test

# Step 6: Start server
./mvnw spring-boot:run

# Step 7: Test API
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

---

## 📊 IMPLEMENTATION PROGRESS

```
Database Schema         ✅ DONE  (SQL scripts ready)
Entity Classes          ✅ DONE  (User, Staff updated)
Repositories            ✅ DONE  (UserRepository enhanced)
Security Config         ✅ DONE  (Authorization updated)
Service Layer           ✅ DONE  (Auto User creation)
Authorization Service   ✅ DONE  (RestaurantAccessService)
Compilation             ✅ DONE  (BUILD SUCCESS)

Controllers             ⏳ TODO  (Add @PreAuthorize)
Database Migration      ⏳ TODO  (Run SQL script)
Testing                 ⏳ TODO  (Verify all flows)
Deployment              ⏳ TODO  (Package & release)

Status: 67% Complete (7/10 steps done)
Estimated Time to Completion: 2-3 hours
```

---

## ❓ COMMON QUESTIONS

**Q: Do I need to update EVERY controller?**  
A: Priority order:
1. Public endpoints (WaitlistController) 
2. Restaurant operations (RestaurantController)
3. Staff management (AdminStaffController)
4. System admin (Admin endpoints)

**Q: What if I have existing RESTAURANT users?**  
A: Migration script handles this - converts RESTAURANT to OWNER automatically

**Q: Does RestaurantAccessService need to be in a specific package?**  
A: No, but keep it in `/service/` directory alongside other services

**Q: Can I test without running database migration?**  
A: No - UserRole enum must match database. Migration is required.

**Q: How do I test ADMIN access?**  
A: Create admin user with restaurantId=NULL:
```sql
INSERT INTO users (email, password, name, role, restaurant_id, email_verified, enabled)
VALUES ('admin@example.com', '$2a$10$...', 'Admin', 'ADMIN', NULL, true, true);
```

**Q: What if staff invitation doesn't create User?**  
A: Check:
1. UserRepository is injected in AdminStaffServiceImpl
2. AdminStaffServiceImpl.setStaffPassword() calls userRepository.save()
3. No exceptions in logs

---

## 📞 QUICK HELP

**Build Issues?** → Run: `./mvnw clean compile -e`  
**Role Error?** → Check: UserRole enum in User.java  
**Access Denied?** → Verify: @PreAuthorize annotations on endpoint  
**User not created?** → Check: setStaffPassword() flow in AdminStaffServiceImpl  
**Database error?** → Verify: Migration script ran successfully  

---

**Last Updated:** September 19, 2026 16:17 IST  
**Status:** ✅ Implementation Ready for Testing Phase


