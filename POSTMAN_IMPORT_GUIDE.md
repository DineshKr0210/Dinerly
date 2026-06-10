# 📮 Postman Collection - Import Guide

## 📦 Files Provided

Two files have been created for you:

1. **Waitlist-API-Postman-Collection.json** - Complete API collection with all 17 endpoints
2. **Waitlist-API-Environment.json** - Environment variables and test credentials

---

## 🚀 How to Import into Postman

### Step 1: Download the Collection File
```
Location: /Users/dineshkumar/Downloads/backend/Waitlist-API-Postman-Collection.json
```

### Step 2: Import Collection
1. Open **Postman** application
2. Click **Import** button (top-left)
3. Select **File** option
4. Choose `Waitlist-API-Postman-Collection.json`
5. Click **Import**

✅ You should now see "Waitlist Management Backend API" collection in the left sidebar

### Step 3: Import Environment
1. Click **Environment** (top-right dropdown)
2. Click **Import**
3. Choose `Waitlist-API-Environment.json`
4. Select the environment: "Waitlist Backend - Local Development"

✅ You should now see all variables loaded

---

## 📋 Collection Structure

The collection is organized into 4 main folders:

### 🔐 Authentication (3 APIs)
- **Login** - Get JWT token
- **Forgot Password** - Request password reset
- **Reset Password** - Complete password reset

### 👤 Guest Operations (4 APIs)
- **Join Waitlist** - Guest joins waitlist
- **Check Status** - Guest checks position
- **Leave Waitlist** - Guest cancels
- **Submit Feedback** - Guest provides rating

### 🍽️ Restaurant Operations (6+ APIs)
- **Get Waitlist** - View all guests
- **Add Guest** - Manually add guest
- **Notify Guest** - Send SMS
- **Seat Guest** - Mark seated
- **Remove Guest** - Cancel guest
- **Get Tables** - View all tables
- **Update Table** - Change table status

### 📊 Admin Analytics (3 APIs)
- **Dashboard Analytics** - Key metrics
- **Guest History** - Guest patterns
- **Feedback Insights** - Feedback trends

---

## 🔑 Environment Variables

Pre-configured variables in the environment:

```
baseUrl              = http://localhost:8080/api
token                = (empty - fill after login)
restaurantId         = 1
waitlistId           = 101
tableId              = 1
guestPhone           = 9876543210
guestEmail           = guest@example.com
guestPassword        = 123456
restaurantEmail      = restaurant@example.com
restaurantPassword   = 123456
adminEmail           = admin@example.com
adminPassword        = 123456
```

---

## 🧪 Quick Testing Guide

### 1. First-Time Login (REQUIRED)
1. Go to **Authentication** → **Login**
2. Click **Send**
3. In the response, copy the `token` value
4. Go to **Environment Variables** (top-right)
5. Paste the token into `token` variable
6. Click **Save**

✅ Now all authenticated requests will use your token

### 2. Test Guest Flow
```
1. Login (get token)
2. Join Waitlist (POST /waitlist)
3. Check Status (GET /waitlist/status?phone=9876543210)
4. Leave Waitlist (DELETE /waitlist/101)
```

### 3. Test Restaurant Flow
```
1. Login as restaurant
2. Get Waitlist (see all guests)
3. Add Guest (manually add someone)
4. Notify Guest (send SMS)
5. Seat Guest (mark as seated)
6. Get Tables (view table status)
```

### 4. Test Admin Flow
```
1. Login as admin
2. Get Analytics
3. View Guest History
4. Check Feedback Insights
```

---

## 📝 Request Format Examples

### Authentication Request
```json
POST /auth/login
Content-Type: application/json

{
  "email": "guest@example.com",
  "password": "123456"
}
```

### Join Waitlist Request
```json
POST /waitlist
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "restaurantId": 1,
  "name": "Dinesh",
  "phone": "9876543210",
  "partySize": 3,
  "preference": "Indoor",
  "notes": "Birthday celebration"
}
```

### Add Guest Request (Restaurant)
```json
POST /restaurants/1/waitlist
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "Ravi",
  "phone": "9999999999",
  "partySize": 2,
  "preference": "Patio",
  "notes": "VIP Guest",
  "position": 6,
  "estimatedWaitTime": 30
}
```

---

## 🔑 How to Update Token After Login

### Option 1: Manual (Easiest)
1. After login, copy token from response
2. Click **Environment** (top-right)
3. Find `token` variable
4. Paste the token value
5. Click **Save**

### Option 2: Automatic (Using Pre-request Script)
Add this script to automatically capture token after login:

```javascript
// In Login request → Tests tab
if (pm.response.code === 200) {
    let jsonData = pm.response.json();
    pm.environment.set("token", jsonData.data.token);
}
```

---

## 🔗 API Response Examples

### Successful Login (200 OK)
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
      "id": 1,
      "email": "guest@example.com",
      "name": "Dinesh",
      "phone": "9876543210",
      "role": "GUEST"
    }
  }
}
```

### Successful Join Waitlist (201 Created)
```json
{
  "success": true,
  "message": "Successfully joined waitlist",
  "data": {
    "id": 101,
    "guestName": "Dinesh",
    "guestPhone": "9876543210",
    "partySize": 3,
    "preference": "Indoor",
    "notes": "Birthday celebration",
    "status": "PENDING",
    "position": null,
    "estimatedWaitTime": null
  }
}
```

### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Validation error: Phone: must not be blank",
  "data": null
}
```

---

## ✅ Testing Checklist

- [ ] Application running on port 8080
- [ ] Collection imported successfully
- [ ] Environment imported successfully
- [ ] Login endpoint returns token
- [ ] Token is copied to environment variable
- [ ] Protected endpoints work with token
- [ ] Public endpoints work without token
- [ ] SMS/Email services configured
- [ ] Database connection working
- [ ] All 17 endpoints respond correctly

---

## 🆘 Troubleshooting

### "Cannot GET /api/auth/login"
**Issue:** Application not running  
**Solution:** Start the app with `./mvnw spring-boot:run`

### "401 Unauthorized"
**Issue:** Token is missing or invalid  
**Solution:** 
1. Login again to get fresh token
2. Update the `token` environment variable
3. Make sure "Authorization" header has "Bearer {{token}}"

### "CORS error"
**Issue:** Frontend requesting from different origin  
**Solution:** CORS is already configured for "*" in SecurityConfig

### "Connection refused"
**Issue:** Cannot connect to database  
**Solution:** Verify Supabase credentials in application.properties

### "Email not sending"
**Issue:** SMTP credentials incorrect  
**Solution:** Use Gmail App Password (not regular password)

### "SMS not sending"
**Issue:** Twilio credentials incorrect  
**Solution:** Verify Account SID, Auth Token, and phone number format

---

## 📊 Full API Endpoint Reference

| # | Method | Endpoint | Role | Purpose |
|----|--------|----------|------|---------|
| 1 | POST | `/auth/login` | Public | Login |
| 2 | POST | `/auth/forgot-password` | Public | Forgot password |
| 3 | POST | `/auth/reset-password` | Public | Reset password |
| 4 | POST | `/waitlist` | GUEST | Join waitlist |
| 5 | GET | `/waitlist/status?phone=` | Public | Check status |
| 6 | DELETE | `/waitlist/{id}` | Public | Leave waitlist |
| 7 | POST | `/feedback` | GUEST | Submit feedback |
| 8 | GET | `/restaurants/{id}/waitlist` | RESTAURANT | Get waitlist |
| 9 | POST | `/restaurants/{id}/waitlist` | RESTAURANT | Add guest |
| 10 | POST | `/restaurants/waitlist/{id}/notify` | RESTAURANT | Notify guest |
| 11 | POST | `/restaurants/waitlist/{id}/seat` | RESTAURANT | Seat guest |
| 12 | DELETE | `/restaurants/waitlist/{id}` | RESTAURANT | Remove guest |
| 13 | GET | `/restaurants/{id}/tables` | RESTAURANT | Get tables |
| 14 | PUT | `/tables/{id}` | RESTAURANT | Update table |
| 15 | GET | `/admin/analytics` | ADMIN | Analytics |
| 16 | GET | `/admin/guests` | ADMIN | Guest history |
| 17 | GET | `/admin/feedback` | ADMIN | Feedback insights |

---

## 📚 Additional Resources

- **Postman Documentation:** https://learning.postman.com
- **API Documentation:** See `API_DOCUMENTATION.md` in backend folder
- **Setup Guide:** See `SETUP_GUIDE.md` for configuration

---

## 🎯 Next Steps

1. ✅ Import the collection file
2. ✅ Import the environment file
3. ✅ Start the backend application
4. ✅ Login to get a token
5. ✅ Update environment with token
6. ✅ Test all endpoints
7. ✅ Share collection with frontend team

---

## 📞 Need Help?

**Issue:** Collection not importing
**Solution:** Make sure the JSON file is valid and format is correct

**Issue:** Environment variables not working
**Solution:** Use {{variableName}} syntax in requests

**Issue:** Requests failing
**Solution:** Check that application is running and database is connected

---

## 🎉 You're Ready!

The Postman collection is fully configured and ready to use. Start by logging in, then test all endpoints sequentially.

**Happy API Testing! 🚀**

---

**Created:** June 9, 2026
**Version:** 1.0.0
**Total Endpoints:** 17

