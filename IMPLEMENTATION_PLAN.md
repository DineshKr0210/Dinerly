# Waitlist Management System - Backend Implementation Plan

## 📋 Overview
Building a production-ready Spring Boot REST API for a restaurant waitlist management system with JWT authentication, role-based access control, and PostgreSQL database integration.

---

## 🏗️ Architecture & Technology Stack

### Backend Framework
- **Spring Boot 3.x** (already initialized)
- **Spring Data JPA** - Database ORM
- **Spring Security** - Authentication & Authorization
- **JWT (JSON Web Tokens)** - Stateless authentication
- **PostgreSQL** - Database
- **Supabase** - Managed PostgreSQL hosting
- **Maven** - Build tool

### Security Implementation
- JWT token generation & validation
- Role-based access control (RBAC)
  - `GUEST` - End users joining waitlist
  - `RESTAURANT` - Restaurant staff managing waitlist
  - `ADMIN` - System administrators with analytics access
- Password encryption (BCrypt)
- Email-based password reset
- CORS configuration

---

## 📊 Database Schema

### 1. **Users Table**
```
id (PK) | email (UNIQUE) | password (encrypted) | name | phone | role | 
createdAt | updatedAt | isActive
```

### 2. **Restaurants Table**
```
id (PK) | userId (FK) | name | address | phone | email | totalTables | 
createdAt | updatedAt
```

### 3. **Waitlist Table**
```
id (PK) | restaurantId (FK) | guestName | guestPhone | partySize | 
preference | notes | status (PENDING/WAITING/NOTIFIED/SEATED/CANCELLED/NO_SHOW) | 
position | estimatedWaitTime | joinedAt | seatedAt | createdAt | updatedAt
```

### 4. **Tables Table**
```
id (PK) | restaurantId (FK) | tableNumber | capacity | status (OPEN/OCCUPIED/RESERVED) | 
createdAt | updatedAt
```

### 5. **Feedback Table**
```
id (PK) | waitlistId (FK) | rating (1-5) | comments | tags (JSON array) | 
createdAt | updatedAt
```

### 6. **PasswordResetToken Table**
```
id (PK) | userId (FK) | token | expiryDate | isUsed | createdAt
```

### 7. **AuditLog Table** (for analytics)
```
id (PK) | restaurantId (FK) | action | details (JSON) | createdAt
```

---

## 🔌 API Endpoints Summary

### Authentication (Public)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/auth/login` | User login with JWT token |
| POST | `/api/auth/forgot-password` | Send password reset email |
| POST | `/api/auth/reset-password` | Reset password with token |

### Guest APIs (GUEST role)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/waitlist` | Join waitlist |
| GET | `/api/waitlist/status` | Check position & wait time |
| DELETE | `/api/waitlist/{id}` | Leave waitlist |

### Restaurant APIs (RESTAURANT role)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/restaurants/{restaurantId}/waitlist` | View all guests in waitlist |
| POST | `/api/restaurants/{restaurantId}/waitlist` | Manually add guest |
| POST | `/api/waitlist/{id}/notify` | Send SMS notification to guest |
| POST | `/api/waitlist/{id}/seat` | Mark guest as seated |
| DELETE | `/api/waitlist/{id}` | Remove guest from waitlist |
| GET | `/api/restaurants/{restaurantId}/tables` | Get all tables |
| PUT | `/api/tables/{id}` | Update table status |

### Feedback APIs (GUEST role - after being seated)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/feedback` | Submit feedback |

### Admin APIs (ADMIN role)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/admin/analytics` | Dashboard metrics |
| GET | `/api/admin/guests` | Guest history & patterns |
| GET | `/api/admin/feedback` | Feedback insights |

---

## 📦 Project Dependencies to Add

```xml
<!-- Spring Security & JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Email Sending -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Lombok (optional, for reducing boilerplate) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

## 🔐 Security & JWT Strategy

### JWT Payload Structure
```json
{
  "sub": "user_id",
  "email": "user@example.com",
  "name": "User Name",
  "role": "GUEST|RESTAURANT|ADMIN",
  "restaurantId": 1,
  "iat": 1234567890,
  "exp": 1234571490
}
```

### Token Lifecycle
- **Access Token**: 24 hours validity
- **Refresh Token**: 7 days validity (optional, if needed)
- **Password Reset Token**: 1 hour validity

### Authorization Flow
1. User logs in with email & password
2. Server validates credentials and generates JWT
3. Client stores JWT in localStorage
4. Client includes JWT in Authorization header for protected requests
5. Server validates JWT and checks user role against endpoint

---

## 📧 Email Service Implementation

### Password Reset Email
- Trigger: User requests forgot password
- Content: Reset link with token valid for 1 hour
- Implementation: Spring Mail with Gmail/SMTP

### Guest Notification SMS (Optional Enhancement)
- Could integrate Twilio or similar for SMS
- For MVP: Email notifications

---

## 📁 Project Structure

```
src/main/java/com/restaurant/waitlist/backend/
├── config/
│   ├── JwtConfig.java
│   ├── SecurityConfig.java
│   └── CorsConfig.java
├── controller/
│   ├── AuthController.java
│   ├── WaitlistController.java
│   ├── RestaurantController.java
│   ├── TableController.java
│   ├── FeedbackController.java
│   └── AdminController.java
├── service/
│   ├── AuthService.java
│   ├── WaitlistService.java
│   ├── RestaurantService.java
│   ├── TableService.java
│   ├── FeedbackService.java
│   ├── AdminService.java
│   ├── EmailService.java
│   └── JwtService.java
├── repository/
│   ├── UserRepository.java
│   ├── RestaurantRepository.java
│   ├── WaitlistRepository.java
│   ├── TableRepository.java
│   ├── FeedbackRepository.java
│   ├── PasswordResetTokenRepository.java
│   └── AuditLogRepository.java
├── entity/
│   ├── User.java
│   ├── Restaurant.java
│   ├── Waitlist.java
│   ├── Table.java
│   ├── Feedback.java
│   ├── PasswordResetToken.java
│   └── AuditLog.java
├── security/
│   ├── JwtFilter.java
│   ├── CustomUserDetailsService.java
│   └── JwtTokenProvider.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── ForgotPasswordRequest.java
│   │   ├── ResetPasswordRequest.java
│   │   ├── JoinWaitlistRequest.java
│   │   ├── AddGuestRequest.java
│   │   └── FeedbackRequest.java
│   └── response/
│       ├── LoginResponse.java
│       ├── WaitlistResponse.java
│       ├── ApiResponse.java
│       └── Other responses...
├── util/
│   └── Constants.java
└── BackendApplication.java
```

---

## 🎯 Implementation Phases

### Phase 1: Foundation (Database & Security)
- [ ] Configure PostgreSQL with Supabase
- [ ] Create entity classes
- [ ] Create repositories
- [ ] Set up JWT configuration & security filters

### Phase 2: Authentication
- [ ] Implement login endpoint
- [ ] Implement password reset with email
- [ ] Add JWT token validation

### Phase 3: Core Waitlist Functionality
- [ ] Guest APIs (join, check status, leave)
- [ ] Restaurant APIs (view waitlist, add guest, seat, remove)
- [ ] Calculate position and estimated wait time

### Phase 4: Additional Features
- [ ] Table management APIs
- [ ] Feedback submission
- [ ] Admin analytics and insights

### Phase 5: Polish & Testing
- [ ] Error handling & validation
- [ ] Logging & monitoring
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Deployment preparation

---

## 🔄 Business Logic Highlights

### Waitlist Position Calculation
- Position = Count of guests with status WAITING ahead of them
- Estimated Time = Position × Average Service Time per Party

### No-Show Handling
- Track guests who don't arrive within 15 minutes of notification
- Calculate no-show rate for analytics

### Guest History
- Track total visits, average wait time, ratings
- Enable loyalty insights for restaurants

---

## ⚙️ Configuration Files

### application.properties
```properties
spring.datasource.url=jdbc:postgresql://db.lpoaptxjywmkrpozxsaa.supabase.co:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=[YOUR-PASSWORD]
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=[Generate-Strong-Secret-Key]
jwt.expiration=86400000

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=[YOUR-EMAIL]
spring.mail.password=[APP-PASSWORD]
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## ✅ Missing Features (Suggested Additions)

1. **SMS Notifications** - Integrate Twilio for SMS alerts instead of just email
2. **Real-time Updates** - WebSocket support for live position updates
3. **Queue Prioritization** - VIP guests, birthdays, special occasions
4. **Waitlist Settings** - Restaurants can configure max wait time, avg service time per party
5. **Analytics Export** - Export reports as CSV/PDF
6. **Restaurant Ratings** - Separate from waitlist feedback
7. **Notification Preferences** - Users choose SMS, email, or push notifications
8. **Queue Merging** - Handle walk-ins vs. reservations
9. **Rate Limiting** - Prevent API abuse
10. **Audit Trail** - Log all admin actions

---

## 📝 Next Steps

1. ✅ Review this implementation plan
2. ⏳ Approve the plan and highlight any changes
3. 🚀 I'll implement the complete backend with all APIs
4. 🧪 Test all endpoints
5. 📚 Provide API documentation & Postman collection

---

**Estimated Implementation Time**: 4-6 hours for complete development

**Are you ready to proceed? Any changes or additions to this plan?**

