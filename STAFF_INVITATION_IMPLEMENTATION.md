# Staff Invitation & Onboarding System - Complete Implementation

## Overview
Complete staff invitation workflow with secure token-based authentication and password setup.

---

## System Flow

### 1. **Admin Invites Staff** (Protected - ADMIN role only)
**Endpoint:** `POST /api/admin/staff`

**Request:**
```json
{
  "name": "John Doe",
  "role": "Manager",
  "email": "john@example.com",
  "locationId": 1
}
```

**What Happens:**
- Staff record created with status `INVITED`
- Unique 24-hour expiring token generated
- Invitation email sent to staff with token link
- Audit log entry created

**Response:**
```json
{
  "statusCode": 200,
  "message": "Staff invitation sent successfully",
  "data": {
    "id": 5,
    "name": "John Doe",
    "role": "Manager",
    "email": "john@example.com",
    "status": "INVITED",
    "locationId": 1,
    "location": "Restaurant Name"
  }
}
```

**Email Content:**
```
You have been invited to join Dinerly as Manager for Restaurant Name.

Click the link below to accept the invitation and set up your account:
http://dev.dinerly.ca/staff/accept-invitation?token=<UNIQUE_TOKEN>

This invitation link will expire in 24 hours.

If you did not expect this invitation, please ignore this email.
```

---

### 2. **Staff Verifies Invitation Token** (Public - No auth required)
**Endpoint:** `POST /api/admin/staff/verify-invitation`

**When:** Staff clicks the invitation link, frontend calls this endpoint to validate token

**Request:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (Valid Token):**
```json
{
  "statusCode": 200,
  "message": "Invitation token verified",
  "data": {
    "valid": true,
    "message": "Invitation token is valid. Please set your password.",
    "staffId": 5,
    "staffName": "John Doe",
    "staffEmail": "john@example.com",
    "restaurantName": "Restaurant Name"
  }
}
```

**Response (Invalid/Expired Token):**
```json
{
  "statusCode": 400,
  "message": "This invitation token has expired",
  "data": null
}
```

---

### 3. **Check Token Status** (Optional - Public endpoint)
**Endpoint:** `POST /api/admin/staff/check-invitation-token`

**Purpose:** Frontend can check if token is still valid without verifying it

**Request:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (Valid Token):**
```json
{
  "statusCode": 200,
  "message": "Token status checked",
  "data": {
    "valid": true,
    "message": "Invitation token is valid",
    "expiresAt": "2026-09-20T14:30:00"
  }
}
```

---

### 4. **Staff Sets Password** (Public - No auth required)
**Endpoint:** `POST /api/admin/staff/set-password`

**After** clicking invitation link and seeing verify response, staff sets their password

**Request:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "password": "SecurePassword123",
  "confirmPassword": "SecurePassword123"
}
```

**Password Requirements:**
- Minimum 8 characters
- Password and confirmation must match
- Passwords are BCrypt encrypted before storing

**Response (Success):**
```json
{
  "statusCode": 200,
  "message": "Password set successfully. Your account is now active!",
  "data": {
    "id": 5,
    "name": "John Doe",
    "role": "Manager",
    "email": "john@example.com",
    "status": "ACTIVE",
    "locationId": 1,
    "location": "Restaurant Name"
  }
}
```

**What Happens on Success:**
- Password encrypted with BCrypt and stored
- Staff status changed from `INVITED` to `ACTIVE`
- Token marked as used (can't be reused)
- Audit log entry created: `STAFF_ACTIVATED_VIA_INVITATION`
- Staff can now login with email and password

**Response (Invalid Password):**
```json
{
  "statusCode": 400,
  "message": "Password must be at least 8 characters long",
  "data": null
}
```

---

## Database Schema

### `staff` Table
```sql
- id (Long, PK)
- restaurant_id (Long, FK - Restaurant)
- name (String)
- role (String)
- email (String)
- password (String) -- NEW: BCrypt encrypted password
- status (ENUM: ACTIVE, INVITED, INACTIVE)
- created_at (LocalDateTime)
- updated_at (LocalDateTime)
```

### `staff_invitation_tokens` Table (NEW)
```sql
- id (Long, PK)
- staff_id (Long, FK - Staff)
- token (String, UNIQUE)
- expiry_date (LocalDateTime) -- 24 hours from creation
- is_used (Boolean, default: false)
- created_at (LocalDateTime)
```

---

## Entity Classes

### `StaffInvitationToken.java`
```java
@Entity
@Table(name = "staff_invitation_tokens")
public class StaffInvitationToken {
    private Long id;
    @OneToOne
    private Staff staff;
    private String token;
    private LocalDateTime expiryDate;
    private Boolean isUsed;
    private LocalDateTime createdAt;
    
    // Validates that token is not used and not expired
    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiryDate);
    }
    
    // Generate unique token using UUID
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
```

### `Staff.java` (Updated)
```java
@Entity
@Table(name = "staff")
public class Staff {
    // ... existing fields ...
    @Column(nullable = true, length = 255)
    private String password;  // NEW: BCrypt encrypted
    // ... rest of class ...
}
```

---

## Service Methods

### `AdminStaffServiceImpl.java`

#### 1. `inviteStaff(AdminStaffRequest request)`
- Creates staff record with `INVITED` status
- Generates token with 24-hour expiry
- Saves token to database
- Sends invitation email
- Returns staff response

#### 2. `verifyInvitationToken(String token)`
- Finds token in database
- Validates token is not used
- Validates token is not expired
- Returns staff details if valid
- Throws exception if invalid/expired

#### 3. `setStaffPassword(StaffSetPasswordRequest request)`
- Validates token is valid
- Validates passwords match
- Validates password length (min 8 chars)
- Encrypts password using BCrypt
- Updates staff status to `ACTIVE`
- Marks token as used
- Saves password to database
- Creates audit log

#### 4. `checkInvitationTokenStatus(String token)`
- Returns token validity without processing
- Shows expiry time if valid

---

## Repository Interfaces

### `StaffInvitationTokenRepository.java` (NEW)
```java
@Repository
public interface StaffInvitationTokenRepository 
        extends JpaRepository<StaffInvitationToken, Long> {
    
    Optional<StaffInvitationToken> findByToken(String token);
    
    Optional<StaffInvitationToken> findByStaffIdAndIsUsedFalse(Long staffId);
}
```

---

## New DTOs

### Request DTOs
1. **`StaffVerifyInvitationRequest`**
   - `token` (String) - Invitation token

2. **`StaffSetPasswordRequest`**
   - `token` (String) - Invitation token  
   - `password` (String) - New password (min 8 chars)
   - `confirmPassword` (String) - Password confirmation

### Response DTOs
1. **`StaffTokenVerificationResponse`**
   - `valid` (Boolean)
   - `message` (String)
   - `staffId` (Long)
   - `staffName` (String)
   - `staffEmail` (String)
   - `restaurantName` (String)

---

## Email Configuration

### New Email Method in `EmailService.java`
```java
public void sendStaffInvitationEmail(
    String toEmail, 
    String staffName, 
    String restaurantName, 
    String invitationToken)
```

**Frontend URL:** `/staff/accept-invitation?token=`

---

## Frontend Integration Flow

```
1. Admin invites staff
   ↓
2. Staff receives email with link
   ↓
3. Staff clicks link → Frontend extracts token from URL
   ↓
4. Frontend calls POST /api/admin/staff/verify-invitation
   ↓
5. If valid → Show password setup form
   ↓
6. Staff enters password → Frontend calls POST /api/admin/staff/set-password
   ↓
7. If successful → Show success message, redirect to login
   ↓
8. Staff logs in with email & password
```

---

## Security Features

✅ **Token-Based:** Unique UUID tokens for each invitation
✅ **Time-Limited:** Tokens expire after 24 hours
✅ **One-Time Use:** Tokens can only be used once
✅ **Password Encrypted:** BCrypt encryption for passwords
✅ **Audit Trail:** All actions logged to audit_logs table
✅ **Public Endpoints:** Invitation/password endpoints don't require authentication
✅ **Admin Protected:** Only admins can invite staff

---

## Error Handling

### Common Error Responses

**Invalid Token:**
```json
{
  "statusCode": 400,
  "message": "Invalid invitation token",
  "data": null
}
```

**Token Already Used:**
```json
{
  "statusCode": 400,
  "message": "This invitation has already been used",
  "data": null
}
```

**Token Expired:**
```json
{
  "statusCode": 400,
  "message": "This invitation token has expired",
  "data": null
}
```

**Password Mismatch:**
```json
{
  "statusCode": 400,
  "message": "Passwords do not match",
  "data": null
}
```

**Password Too Weak:**
```json
{
  "statusCode": 400,
  "message": "Password must be at least 8 characters long",
  "data": null
}
```

---

## Audit Log Entries

```
STAFF_INVITED
  → "Invited John Doe (john@example.com) as Manager"

STAFF_ACTIVATED_VIA_INVITATION
  → "Staff John Doe (john@example.com) activated their account"
```

---

## Testing

### Postman Collection Example

```
1. Invite Staff
   POST http://localhost:8080/api/admin/staff
   Headers: Authorization: Bearer <ADMIN_TOKEN>
   Body: { name, role, email, locationId }

2. Verify Token
   POST http://localhost:8080/api/admin/staff/verify-invitation
   Body: { token: "<TOKEN_FROM_EMAIL>" }

3. Check Token Status
   POST http://localhost:8080/api/admin/staff/check-invitation-token
   Body: { token: "<TOKEN>" }

4. Set Password
   POST http://localhost:8080/api/admin/staff/set-password
   Body: { token, password, confirmPassword }
```

---

## Summary

✅ **Complete Implementation:**
- ✅ Proper token generation (UUID-based)
- ✅ Token validation and expiry
- ✅ Password setup with validation
- ✅ Email notification with secure link
- ✅ Database persistence
- ✅ Audit logging
- ✅ Error handling
- ✅ Security best practices
- ✅ Public endpoints for invitation flow
- ✅ Status transitions (INVITED → ACTIVE)

**Ready for production!** 🚀

