# 📮 POSTMAN COLLECTION - READY TO IMPORT

## ✅ What You Got

I've created a complete Postman collection with all 17 API endpoints ready to import directly into Postman!

---

## 📂 Files Created

### 1. **Waitlist-API-Postman-Collection.json**
- Complete API collection with all 17 endpoints
- Pre-configured request templates
- Response examples for each endpoint
- Organized in 4 folders:
  - 🔐 Authentication (3 APIs)
  - 👤 Guest Operations (4 APIs)
  - 🍽️ Restaurant Operations (6+ APIs)
  - 📊 Admin Analytics (3 APIs)

### 2. **Waitlist-API-Environment.json**
- Pre-configured environment variables
- Test credentials for all 3 roles
- URL and token variables
- Easy-to-modify values

### 3. **POSTMAN_IMPORT_GUIDE.md**
- Step-by-step import instructions
- How to set up and use the collection
- Request format examples
- Troubleshooting guide

---

## 🚀 Quick Import Steps

### Step 1: Download Files
```
Files located at: /Users/dineshkumar/Downloads/backend/
- Waitlist-API-Postman-Collection.json
- Waitlist-API-Environment.json
```

### Step 2: Import Collection
1. Open **Postman**
2. Click **Import** (top-left)
3. Select `Waitlist-API-Postman-Collection.json`
4. Click **Import**

### Step 3: Import Environment
1. Click **Environment** dropdown (top-right)
2. Click **Import**
3. Select `Waitlist-API-Environment.json`

### Step 4: Select Environment
1. Click **Environment** dropdown
2. Select **"Waitlist Backend - Local Development"**

✅ **You're ready to go!**

---

## 📋 Collection Overview

### Folder 1: 🔐 Authentication (3 APIs)
```
1. Login
   POST /api/auth/login
   - Request: { email, password }
   - Response: { token, user }

2. Forgot Password
   POST /api/auth/forgot-password
   - Request: { email }
   - Response: Success message

3. Reset Password
   POST /api/auth/reset-password
   - Request: { token, newPassword, confirmPassword }
   - Response: Success message
```

### Folder 2: 👤 Guest Operations (4 APIs)
```
4. Join Waitlist (GUEST role)
   POST /api/waitlist
   - Sets status to PENDING

5. Check Waitlist Status (Public)
   GET /api/waitlist/status?phone=
   - Shows position & wait time

6. Leave Waitlist (Public)
   DELETE /api/waitlist/{id}
   - Cancels waitlist entry

7. Submit Feedback (GUEST role)
   POST /api/feedback
   - Rating 1-5, comments, tags
```

### Folder 3: 🍽️ Restaurant Operations (6+ APIs)
```
8. Get Waitlist (RESTAURANT role)
   GET /api/restaurants/{id}/waitlist
   - Lists all guests

9. Add Guest (RESTAURANT role)
   POST /api/restaurants/{id}/waitlist
   - Add with position & wait time

10. Notify Guest (RESTAURANT role)
    POST /api/restaurants/waitlist/{id}/notify
    - Sends SMS notification

11. Seat Guest (RESTAURANT role)
    POST /api/restaurants/waitlist/{id}/seat
    - Marks guest as SEATED

12. Remove Guest (RESTAURANT role)
    DELETE /api/restaurants/waitlist/{id}
    - Removes from waitlist

13. Get Tables (RESTAURANT role)
    GET /api/restaurants/{id}/tables
    - Lists all restaurant tables

14. Update Table Status (RESTAURANT role)
    PUT /api/tables/{id}
    - Changes table status
```

### Folder 4: 📊 Admin Analytics (3 APIs)
```
15. Dashboard Analytics (ADMIN role)
    GET /api/admin/analytics
    - Metrics & KPIs

16. Guest History (ADMIN role)
    GET /api/admin/guests
    - Guest patterns & history

17. Feedback Insights (ADMIN role)
    GET /api/admin/feedback
    - Feedback trends & tags
```

---

## 🔑 Pre-Configured Variables

| Variable | Default Value | Purpose |
|----------|---------------|---------|
| baseUrl | http://localhost:8080/api | API base URL |
| token | (empty) | JWT token - fill after login |
| restaurantId | 1 | Sample restaurant ID |
| waitlistId | 101 | Sample waitlist entry ID |
| tableId | 1 | Sample table ID |
| guestPhone | 9876543210 | Sample guest phone |
| guestEmail | guest@example.com | Test guest email |
| guestPassword | 123456 | Test guest password |
| restaurantEmail | restaurant@example.com | Test restaurant email |
| restaurantPassword | 123456 | Test restaurant password |
| adminEmail | admin@example.com | Test admin email |
| adminPassword | 123456 | Test admin password |

---

## 🧪 Testing Workflow

### 1. Get JWT Token
```
Step 1: Go to Authentication → Login
Step 2: Click Send
Step 3: Copy token from response
Step 4: Update baseUrl environment variable with token
```

### 2. Test Guest Flow
```
Step 1: Join Waitlist (POST /waitlist)
Step 2: Check Waitlist Status (GET /waitlist/status)
Step 3: Leave Waitlist (DELETE /waitlist/{id})
```

### 3. Test Restaurant Flow
```
Step 1: Get Waitlist (GET /restaurants/{id}/waitlist)
Step 2: Add Guest (POST /restaurants/{id}/waitlist)
Step 3: Notify Guest (POST /restaurants/waitlist/{id}/notify)
Step 4: Seat Guest (POST /restaurants/waitlist/{id}/seat)
```

### 4. Test Admin Flow
```
Step 1: Get Analytics (GET /admin/analytics)
Step 2: Get Guest History (GET /admin/guests)
Step 3: Get Feedback (GET /admin/feedback)
```

---

## 📝 Request Examples

### Login Request
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "guest@example.com",
  "password": "123456"
}
```

### Join Waitlist Request
```
POST http://localhost:8080/api/waitlist
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

### Add Guest Request
```
POST http://localhost:8080/api/restaurants/1/waitlist
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

## ✅ What Works

✅ All 17 API endpoints  
✅ Organized in 4 logical folders  
✅ Pre-configured variables  
✅ Sample request bodies  
✅ Response examples  
✅ Authorization headers  
✅ Public & protected endpoints  
✅ Error handling  

---

## 🎯 How to Use

1. **First Login**
   - Go to Authentication → Login
   - Get your JWT token
   - Update the `token` environment variable

2. **Test Public APIs**
   - Can call without token:
     - `/api/auth/login`
     - `/api/auth/forgot-password`
     - `/api/auth/reset-password`
     - `/api/waitlist/status`

3. **Test Protected APIs**
   - Requires token in Authorization header:
     - All Guest, Restaurant, and Admin APIs
   - Make sure token is set in environment variables

4. **Update Variables**
   - Click **Environment** (top-right)
   - Update any values you need
   - Click **Save**

---

## 🆘 Common Issues

### "401 Unauthorized"
**Problem:** Token is invalid or missing  
**Solution:** 
1. Login again to get a fresh token
2. Copy new token to environment variable
3. Make sure Authorization header is set

### "404 Not Found"
**Problem:** API endpoint not found  
**Solution:**
1. Check baseUrl is correct
2. Make sure application is running
3. Verify endpoint path

### "Network Error"
**Problem:** Cannot connect to server  
**Solution:**
1. Start backend: `./mvnw spring-boot:run`
2. Verify baseUrl: `http://localhost:8080/api`
3. Check that port 8080 is available

### "CORS Error"
**Problem:** Frontend blocked by CORS  
**Solution:**
1. CORS is configured for "*" in the backend
2. Check that requests are from allowed origin
3. Verify Content-Type header is set

---

## 📚 Documentation

For more details, see:
- **POSTMAN_IMPORT_GUIDE.md** - Detailed import instructions
- **API_DOCUMENTATION.md** - Complete API reference
- **SETUP_GUIDE.md** - Configuration help
- **README.md** - Project overview

---

## 🚀 Next Steps

1. ✅ Import collection into Postman
2. ✅ Import environment into Postman
3. ✅ Start backend application
4. ✅ Login to get JWT token
5. ✅ Test all 17 endpoints
6. ✅ Share collection with frontend team

---

## 📮 File Locations

| File | Location |
|------|----------|
| Collection | `/Users/dineshkumar/Downloads/backend/Waitlist-API-Postman-Collection.json` |
| Environment | `/Users/dineshkumar/Downloads/backend/Waitlist-API-Environment.json` |
| Import Guide | `/Users/dineshkumar/Downloads/backend/POSTMAN_IMPORT_GUIDE.md` |

---

## ✨ Features

✅ Complete API coverage (17 endpoints)  
✅ All request/response examples  
✅ Pre-configured variables  
✅ Role-based test data  
✅ Easy to import & use  
✅ Ready for team collaboration  
✅ Works offline after import  

---

## 🎉 Summary

You now have a complete, ready-to-use Postman collection that covers all 17 API endpoints for your restaurant waitlist management system!

**Status:** ✅ Ready to Import  
**Date Created:** June 9, 2026  
**Total Endpoints:** 17  
**Collection Version:** 1.0.0  

---

**Happy Testing! 🚀**

Import the files now and start testing your APIs!

