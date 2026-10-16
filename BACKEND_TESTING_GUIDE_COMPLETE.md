# Backend Testing Guide - Both Scenarios
## Step-by-Step Testing for Scenario 1 & Scenario 2

**Date:** October 16, 2026  
**Purpose:** Complete backend testing guide for both server assignment scenarios  
**Status:** Ready for QA Testing

---

## Table of Contents

1. [Pre-Testing Setup](#pre-testing-setup)
2. [Scenario 1 Testing (Fair Rotation)](#scenario-1-testing-fair-rotation)
3. [Scenario 2 Testing (Section-Based)](#scenario-2-testing-section-based)
4. [Integration Testing](#integration-testing)
5. [Edge Cases & Error Testing](#edge-cases--error-testing)
6. [Performance Testing](#performance-testing)

---

## Pre-Testing Setup

### Prerequisites
- ✅ Backend running on `http://localhost:8080`
- ✅ Database connected and migrated (V10 should run)
- ✅ Postman or curl installed
- ✅ Valid JWT tokens for testing

### Test Data to Create

**Step 1: Create a Restaurant**
```bash
curl -X POST http://localhost:8080/api/admin/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Restaurant",
    "address": "123 Test St",
    "phone": "555-1234",
    "email": "test@restaurant.com",
    "totalTables": 10
  }'
# Save restaurantId → RESTAURANT_ID
```

**Step 2: Create 5 Tables**
```bash
for i in 1 2 3 4 5; do
  curl -X POST http://localhost:8080/api/restaurants/RESTAURANT_ID/tables \
    -H "Authorization: Bearer TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"tableNumber\": \"Table $i\",
      \"capacity\": 4
    }"
done
# Save table IDs → TABLE_1, TABLE_2, TABLE_3, TABLE_4, TABLE_5
```

**Step 3: Create 3 Servers**
```bash
for i in 1 2 3; do
  curl -X POST http://localhost:8080/api/admin/staff \
    -H "Authorization: Bearer TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"Server $i\",
      \"role\": \"SERVER\",
      \"locationId\": RESTAURANT_ID,
      \"email\": \"server$i@restaurant.com\"
    }"
done
# Save staff IDs → SERVER_1, SERVER_2, SERVER_3
```

**Step 4: Make Servers Set Their Password**
```bash
# Use the invitation token from email (or get from staff_invitation_tokens table)
curl -X POST http://localhost:8080/api/admin/staff/set-password \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"INVITATION_TOKEN\",
    \"password\": \"Passw0rd@123\",
    \"confirmPassword\": \"Passw0rd@123\"
  }"
```

**Step 5: Get JWT Tokens**
```bash
# Admin/Manager Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@restaurant.com",
    "password": "adminpass"
  }'
# Save → TOKEN_ADMIN

# Server Tokens
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "server1@restaurant.com",
    "password": "Passw0rd@123"
  }'
# Save → TOKEN_SERVER_1, TOKEN_SERVER_2, TOKEN_SERVER_3 (repeat for other servers)
```

### Environment Variables
```bash
export RESTAURANT_ID="1"
export TOKEN_ADMIN="your_admin_token"
export TOKEN_SERVER_1="server1_token"
export TOKEN_SERVER_2="server2_token"
export TOKEN_SERVER_3="server3_token"
export TABLE_1="1"
export TABLE_2="2"
export TABLE_3="3"
export TABLE_4="4"
export TABLE_5="5"
export SERVER_1="1"
export SERVER_2="2"
export SERVER_3="3"
```

---

# SCENARIO 1 TESTING: Fair Rotation

## Test Suite: Scenario 1 (Round-Robin Rotation)

### Scenario 1 - Test 1: Check Default Setting
**Purpose:** Verify Scenario 1 is the default mode

**Step 1:** Get restaurant settings
```bash
curl -X GET http://localhost:8080/api/settings/$RESTAURANT_ID/advanced \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
```json
{
  "success": true,
  "data": {
    "serverAssignmentMode": "ROTATION",
    "darkMode": false,
    "timezone": "Canada/Central",
    ...
  }
}
```

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 1 - Test 2: Put Servers on Floor
**Purpose:** Add servers to the rotation queue in order

**Step 1:** Add Server 1 to floor
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/$SERVER_1/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"onFloor": true}'
```

**Expected Result:**
```json
{
  "success": true,
  "data": [
    {
      "staffId": 1,
      "name": "Server 1",
      "onFloor": true,
      "seatedCount": 0,
      "rotationOrder": 1,
      "nextUp": true
    }
  ]
}
```

**Verify:** 
- ✅ Server 1 is onFloor = true
- ✅ rotationOrder = 1
- ✅ nextUp = true (first server)

**Step 2:** Add Server 2 to floor
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/$SERVER_2/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"onFloor": true}'
```

**Expected Result:**
```json
{
  "success": true,
  "data": [
    {
      "staffId": 1,
      "name": "Server 1",
      "onFloor": true,
      "seatedCount": 0,
      "rotationOrder": 1,
      "nextUp": true
    },
    {
      "staffId": 2,
      "name": "Server 2",
      "onFloor": true,
      "seatedCount": 0,
      "rotationOrder": 2,
      "nextUp": false
    }
  ]
}
```

**Verify:**
- ✅ Both servers onFloor
- ✅ Server 1 still nextUp
- ✅ Server 2 in rotation order 2

**Step 3:** Add Server 3 to floor
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/$SERVER_3/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"onFloor": true}'
```

**Expected Result:** All 3 servers on floor, Server 1 nextUp

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 1 - Test 3: View Floor Roster
**Purpose:** Verify rotation roster is correct

**Step:** Get floor roster
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
```json
{
  "success": true,
  "data": [
    { "staffId": 1, "name": "Server 1", "onFloor": true, "seatedCount": 0, "rotationOrder": 1, "nextUp": true },
    { "staffId": 2, "name": "Server 2", "onFloor": true, "seatedCount": 0, "rotationOrder": 2, "nextUp": false },
    { "staffId": 3, "name": "Server 3", "onFloor": true, "seatedCount": 0, "rotationOrder": 3, "nextUp": false }
  ]
}
```

**Verify:**
- ✅ 3 servers listed
- ✅ Server 1 nextUp = true
- ✅ All seatedCount = 0
- ✅ rotationOrder is correct

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 1 - Test 4: Create Waitlist Guests
**Purpose:** Create guests to seat

**Step 1:** Add Guest 1
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Guest 1",
    "phone": "555-0001",
    "partySize": 2
  }'
# Save waitlistId → GUEST_1
```

**Step 2:** Add Guest 2
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Guest 2",
    "phone": "555-0002",
    "partySize": 2
  }'
# Save → GUEST_2
```

**Step 3:** Add Guest 3
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Guest 3",
    "phone": "555-0003",
    "partySize": 2
  }'
# Save → GUEST_3
```

**Step 4:** Add Guest 4
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Guest 4",
    "phone": "555-0004",
    "partySize": 2
  }'
# Save → GUEST_4
```

**Expected Result:** Each returns 201 with waitlist entry

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 1 - Test 5: IN-TURN Seating (Auto Next Up)
**Purpose:** Seat Guest 1 without specifying server → should go to Server 1 (Next Up)

**Step 1:** Seat Guest 1 (no server specified)
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_1/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": '$TABLE_1'
  }'
```

**Expected Result:**
```json
{
  "success": true,
  "data": {
    "id": GUEST_1,
    "guestName": "Guest 1",
    "status": "SEATED",
    "assignedServerId": 1,  ← SERVER 1
    "tableId": 1,
    "seatedAt": "2026-10-16T..."
  }
}
```

**Verify:**
- ✅ assignedServerId = 1 (Server 1)
- ✅ status = SEATED
- ✅ tableId = 1

**Step 2:** Check floor roster (pointer should advance)
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
```json
{
  "data": [
    { "staffId": 1, "seatedCount": 1, "nextUp": false, ...},  ← Count increased
    { "staffId": 2, "seatedCount": 0, "nextUp": true, ...},   ← Now nextUp
    { "staffId": 3, "seatedCount": 0, "nextUp": false, ...}
  ]
}
```

**Verify:**
- ✅ Server 1 seatedCount = 1
- ✅ Server 2 is now nextUp = true
- ✅ Pointer advanced

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 1 - Test 6: IN-TURN Seating (Server 2)
**Purpose:** Seat Guest 2 without server → should go to Server 2 (currently Next Up)

**Step 1:** Seat Guest 2
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_2/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": '$TABLE_2'
  }'
```

**Expected Result:**
```json
{
  "data": {
    "assignedServerId": 2,  ← SERVER 2 now
    "status": "SEATED"
  }
}
```

**Step 2:** Check roster
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
- ✅ Server 2 seatedCount = 1
- ✅ Server 3 is now nextUp = true

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 1 - Test 7: OUT-OF-TURN Seating (Fairness Preservation)
**Purpose:** Seat Guest 3 with OVERRIDE → assign to Server 1 (not Server 3 who is nextUp) → pointer should STAY on Server 3

**Step 1:** Seat Guest 3 with server override
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_3/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": '$TABLE_3',
    "serverId": '$SERVER_1'
  }'
```

**Expected Result:**
```json
{
  "data": {
    "assignedServerId": 1,  ← SERVER 1 (out-of-turn)
    "status": "SEATED"
  }
}
```

**Step 2:** Check roster - **CRITICAL TEST**
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result - MUST BE TRUE:**
```json
{
  "data": [
    { "staffId": 1, "seatedCount": 2, "nextUp": false, ...},   ← Count increased
    { "staffId": 2, "seatedCount": 1, "nextUp": false, ...},
    { "staffId": 3, "seatedCount": 0, "nextUp": true, ...}     ← STILL nextUp!
  ]
}
```

**CRITICAL VERIFY:**
- ✅ Server 1 seatedCount = 2 (increased)
- ✅ Server 3 nextUp = true (NOT advanced, fairness preserved)
- ✅ Pointer did NOT skip over Server 3

**Test Result:** ✅ PASS / ❌ FAIL  
**This is the core Scenario 1 fairness guarantee!**

---

### Scenario 1 - Test 8: IN-TURN Seating (Server 3)
**Purpose:** Next in-turn seating should go to Server 3 (who was skipped)

**Step 1:** Seat Guest 4 without server
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_4/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": '$TABLE_4'
  }'
```

**Expected Result:**
```json
{
  "data": {
    "assignedServerId": 3  ← Server 3 gets it (was skipped, now gets turn)
  }
}
```

**Step 2:** Check roster
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
- ✅ Server 3 seatedCount = 1
- ✅ Server 1 is now nextUp = true (pointer cycled back)

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 1 - Test 9: Start New Shift
**Purpose:** Manager resets seatedCount and rotation

**Step:** Call shift start
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/shift/start \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
```json
{
  "data": [
    { "staffId": 1, "seatedCount": 0, "nextUp": true, "rotationOrder": 1, ...},
    { "staffId": 2, "seatedCount": 0, "nextUp": false, "rotationOrder": 2, ...},
    { "staffId": 3, "seatedCount": 0, "nextUp": false, "rotationOrder": 3, ...}
  ]
}
```

**Verify:**
- ✅ All seatedCount = 0
- ✅ First server is nextUp again
- ✅ rotationOrder reset

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 1 - Test 10: Remove Server from Floor
**Purpose:** Server goes on break → pointer adjusts

**Step 1:** Remove Server 2 from floor
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/$SERVER_2/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"onFloor": false}'
```

**Expected Result:**
```json
{
  "data": [
    { "staffId": 1, "onFloor": true, "nextUp": true, ...},
    { "staffId": 2, "onFloor": false, ...},
    { "staffId": 3, "onFloor": true, "nextUp": false, ...}
  ]
}
```

**Verify:**
- ✅ Server 2 onFloor = false
- ✅ Only Server 1 and 3 remain active

**Test Result:** ✅ PASS / ❌ FAIL

---

# SCENARIO 2 TESTING: Section-Based Assignment

## Test Suite: Scenario 2 (Table-Based Section Assignment)

### Scenario 2 - Test 1: Switch to SECTION Mode
**Purpose:** Change setting from ROTATION to SECTION

**Step:** Update setting
```bash
curl -X POST http://localhost:8080/api/settings/$RESTAURANT_ID/advanced \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "serverAssignmentMode": "SECTION",
    "darkMode": false,
    "timezone": "Canada/Central"
  }'
```

**Expected Result:**
```json
{
  "success": true,
  "data": {
    "serverAssignmentMode": "SECTION"
  }
}
```

**Verify:**
- ✅ Mode changed to SECTION

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 2: Verify Scenario 2 Is Active
**Purpose:** Confirm setting is persisted

**Step:** Get settings
```bash
curl -X GET http://localhost:8080/api/settings/$RESTAURANT_ID/advanced \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
```json
{
  "data": {
    "serverAssignmentMode": "SECTION"
  }
}
```

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 3: Reset Servers
**Purpose:** Clear old rotation data before testing Scenario 2

**Step 1:** Remove all servers from floor
```bash
for i in 1 2 3; do
  curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/$i/floor \
    -H "Authorization: Bearer $TOKEN_ADMIN" \
    -H "Content-Type: application/json" \
    -d '{"onFloor": false}'
done
```

**Expected Result:**
- ✅ All servers offFloor

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 4: Create Table-Server Assignments
**Purpose:** Assign tables to servers (the core of Scenario 2)

**Step 1:** Assign Table 1 to Server 1
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/$TABLE_1/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "serverId": '$SERVER_1',
    "sectionName": "Window"
  }'
```

**Expected Result:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "tableId": 1,
    "tableName": "Table 1",
    "assignedServerId": 1,
    "serverName": "Server 1",
    "sectionName": "Window",
    "isActive": true
  }
}
```

**Verify:**
- ✅ Assignment created with id
- ✅ tableId = 1
- ✅ assignedServerId = 1
- ✅ sectionName = "Window"

**Step 2:** Assign Table 2 to Server 1 (same section)
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/$TABLE_2/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "serverId": '$SERVER_1',
    "sectionName": "Window"
  }'
```

**Expected Result:**
```json
{
  "data": {
    "tableId": 2,
    "assignedServerId": 1,
    "sectionName": "Window"
  }
}
```

**Step 3:** Assign Table 3 to Server 2
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/$TABLE_3/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "serverId": '$SERVER_2',
    "sectionName": "Bar"
  }'
```

**Step 4:** Assign Table 4 to Server 2 (same section)
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/$TABLE_4/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "serverId": '$SERVER_2',
    "sectionName": "Bar"
  }'
```

**Step 5:** Assign Table 5 to Server 3
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/$TABLE_5/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "serverId": '$SERVER_3',
    "sectionName": "Patio"
  }'
```

**Summary after Step 5:**
- Server 1: Tables 1, 2 (Window section)
- Server 2: Tables 3, 4 (Bar section)
- Server 3: Table 5 (Patio section)

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 5: View All Assignments
**Purpose:** Verify all assignments are stored correctly

**Step:** Get all assignments
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/server-assignments \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
```json
{
  "success": true,
  "data": [
    { "tableId": 1, "assignedServerId": 1, "serverName": "Server 1", "sectionName": "Window", ... },
    { "tableId": 2, "assignedServerId": 1, "serverName": "Server 1", "sectionName": "Window", ... },
    { "tableId": 3, "assignedServerId": 2, "serverName": "Server 2", "sectionName": "Bar", ... },
    { "tableId": 4, "assignedServerId": 2, "serverName": "Server 2", "sectionName": "Bar", ... },
    { "tableId": 5, "assignedServerId": 3, "serverName": "Server 3", "sectionName": "Patio", ... }
  ]
}
```

**Verify:**
- ✅ 5 assignments returned
- ✅ Each has correct tableId, serverId, sectionName
- ✅ All isActive = true

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 6: View Server's Assigned Tables
**Purpose:** Get tables for a specific server

**Step 1:** Get tables for Server 1
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/assigned-tables/$SERVER_1 \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
```json
{
  "data": [
    { "tableId": 1, "tableName": "Table 1", "assignedServerId": 1, "sectionName": "Window", ... },
    { "tableId": 2, "tableName": "Table 2", "assignedServerId": 1, "sectionName": "Window", ... }
  ]
}
```

**Verify:**
- ✅ 2 tables returned (only Server 1's tables)
- ✅ Both in Window section

**Step 2:** Get tables for Server 2
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/assigned-tables/$SERVER_2 \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
- ✅ 2 tables (Tables 3, 4 in Bar section)

**Step 3:** Get tables for Server 3
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/assigned-tables/$SERVER_3 \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
- ✅ 1 table (Table 5 in Patio section)

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 7: Create Fresh Guests for Scenario 2
**Purpose:** Create new guests to test seating with Scenario 2

**Step 1-3:** Create 3 new guests
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Guest A",
    "phone": "555-1111",
    "partySize": 2
  }'
# Save → GUEST_A

# Repeat for Guest B and Guest C
# Save → GUEST_B, GUEST_C
```

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 8: Seat Guest at Table 1 (Server 1)
**Purpose:** Seat guest at Table 1 → should auto-assign to Server 1

**Step:** Seat Guest A at Table 1
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_A/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": '$TABLE_1'
  }'
```

**Expected Result:**
```json
{
  "data": {
    "id": GUEST_A,
    "guestName": "Guest A",
    "status": "SEATED",
    "assignedServerId": 1,  ← TABLE 1's server = Server 1
    "tableId": 1
  }
}
```

**IMPORTANT VERIFY:**
- ✅ assignedServerId = 1 (Server 1, who owns Table 1)
- ✅ No "nextUp" logic needed
- ✅ Automatic based on table assignment

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 9: Seat Guest at Table 3 (Server 2)
**Purpose:** Seat guest at different table → should auto-assign to Server 2

**Step:** Seat Guest B at Table 3
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_B/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": '$TABLE_3'
  }'
```

**Expected Result:**
```json
{
  "data": {
    "assignedServerId": 2  ← TABLE 3's server = Server 2
  }
}
```

**Verify:**
- ✅ assignedServerId = 2
- ✅ Based on Table 3's assignment, not rotation

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 10: Seat Guest at Table 5 (Server 3)
**Purpose:** Verify different section assignment

**Step:** Seat Guest C at Table 5
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_C/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": '$TABLE_5'
  }'
```

**Expected Result:**
```json
{
  "data": {
    "assignedServerId": 3  ← TABLE 5's server = Server 3
  }
}
```

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 11: Seat Guest at Table 1 Again (Reused Table)
**Purpose:** Table 1 becomes available → seated again with Server 1

**Step 1:** Create new guest
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Guest D",
    "phone": "555-2222",
    "partySize": 2
  }'
# Save → GUEST_D
```

**Step 2:** Seat at Table 1
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_D/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": '$TABLE_1'
  }'
```

**Expected Result:**
```json
{
  "data": {
    "assignedServerId": 1  ← Still Server 1 (Table 1's permanent assignment)
  }
}
```

**Verify:**
- ✅ Same server assigned (assignment is permanent)

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 12: Update Table Assignment
**Purpose:** Change which server is assigned to a table

**Step 1:** Reassign Table 1 from Server 1 to Server 2
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/$TABLE_1/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "serverId": '$SERVER_2',
    "sectionName": "Bar-Extended"
  }'
```

**Expected Result:**
```json
{
  "data": {
    "tableId": 1,
    "assignedServerId": 2,  ← Changed from 1 to 2
    "sectionName": "Bar-Extended"
  }
}
```

**Step 2:** Verify change
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/server-assignments \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Verify:**
- ✅ Table 1 now shows Server 2
- ✅ isActive still true

**Test Result:** ✅ PASS / ❌ FAIL

---

### Scenario 2 - Test 13: Deactivate an Assignment
**Purpose:** Remove a table from a server's section

**Step 1:** Get assignment ID for Table 2
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/server-assignments \
  -H "Authorization: Bearer $TOKEN_ADMIN"
# Find the id for Table 2 → ASSIGNMENT_ID
```

**Step 2:** Deactivate the assignment
```bash
curl -X DELETE http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/server-assignments/$ASSIGNMENT_ID \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
```json
{
  "success": true,
  "data": null
}
```

**Step 3:** Verify it's deactivated
```bash
curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/server-assignments \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Verify:**
- ✅ Assignment still exists but isActive = false (soft delete)
- ✅ Other assignments unchanged

**Test Result:** ✅ PASS / ❌ FAIL

---

# INTEGRATION TESTING

## Integration Test 1: Switch Between Scenarios
**Purpose:** Verify system correctly switches logic based on setting

**Step 1:** Set to Scenario 1 (ROTATION)
```bash
curl -X POST http://localhost:8080/api/settings/$RESTAURANT_ID/advanced \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"serverAssignmentMode": "ROTATION"}'
```

**Step 2:** Seat a guest
```bash
# Should use rotation logic
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/X/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -d '{"tableId": 1}'
```

**Expected:** Uses ServerRotationService

**Step 3:** Switch to Scenario 2 (SECTION)
```bash
curl -X POST http://localhost:8080/api/settings/$RESTAURANT_ID/advanced \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"serverAssignmentMode": "SECTION"}'
```

**Step 4:** Seat a guest
```bash
# Should use section assignment logic
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/Y/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -d '{"tableId": 1}'
```

**Expected:** Uses ServerSectionAssignmentService

**Step 5:** Switch back to ROTATION
- Repeat steps 1-2 to confirm switching works

**Test Result:** ✅ PASS / ❌ FAIL

---

## Integration Test 2: Different Restaurants
**Purpose:** Verify settings are per-restaurant

**Step 1:** Create 2 new restaurants (RESTAURANT_2, RESTAURANT_3)

**Step 2:** Set Restaurant 1 to ROTATION
```bash
POST /api/settings/1/advanced { "serverAssignmentMode": "ROTATION" }
```

**Step 3:** Set Restaurant 2 to SECTION
```bash
POST /api/settings/2/advanced { "serverAssignmentMode": "SECTION" }
```

**Step 4:** Verify each has different modes
```bash
GET /api/settings/1/advanced  # Should return ROTATION
GET /api/settings/2/advanced  # Should return SECTION
```

**Test Result:** ✅ PASS / ❌ FAIL

---

## Integration Test 3: Server Notifications
**Purpose:** Verify server gets notified in both scenarios

**For Scenario 1:**
```bash
# Seat a guest (any server gets it from rotation)
POST /waitlist/X/seat

# Server should get notification via ServerNotification table
SELECT * FROM server_notification WHERE server_id = <assigned_server_id>
```

**For Scenario 2:**
```bash
# Seat a guest at Table 1 (Server 1 assigned)
POST /waitlist/X/seat { "tableId": 1 }

# Server 1 should get notification
SELECT * FROM server_notification WHERE server_id = 1
```

**Test Result:** ✅ PASS / ❌ FAIL

---

# EDGE CASES & ERROR TESTING

## Error Test 1: Seat Guest Without Table & Server (Scenario 2)
**Purpose:** Scenario 2 requires tableId

**Step:** Seat with no tableId
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_X/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Expected Result:**
```json
{
  "success": false,
  "message": "Table is required for section-based seating" OR
             "No server assigned to selected table"
}
```

**Test Result:** ✅ PASS / ❌ FAIL

---

## Error Test 2: Assign Server to Table That's Not in Restaurant
**Purpose:** Validation

**Step:**
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/999/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"serverId": 1}'
```

**Expected Result:**
```json
{
  "success": false,
  "message": "Table not found"
}
```

**Test Result:** ✅ PASS / ❌ FAIL

---

## Error Test 3: Assign Non-Existent Server to Table
**Purpose:** Validation

**Step:**
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/$TABLE_1/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"serverId": 999}'
```

**Expected Result:**
```json
{
  "success": false,
  "message": "Staff member not found"
}
```

**Test Result:** ✅ PASS / ❌ FAIL

---

## Error Test 4: Duplicate Table Assignment
**Purpose:** Each table can only be assigned to one server

**Step 1:** Table 1 is assigned to Server 1

**Step 2:** Try reassigning Table 1 value uniquely (should work, not error)
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/$TABLE_1/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"serverId": 2}'
```

**Expected Result:**
```json
{
  "success": true,
  "data": {
    "tableId": 1,
    "assignedServerId": 2  ← Updated, not error
  }
}
```

**Verify:**
- ✅ Update allowed (not insert)
- ✅ New server is now assigned

**Test Result:** ✅ PASS / ❌ FAIL

---

## Error Test 5: No Servers on Floor (Scenario 1)
**Purpose:** Handle gracefully

**Step 1:** Remove all servers from floor

**Step 2:** Try to seat
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_X/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"tableId": 1}'
```

**Expected Result:**
- ✅ assignedServerId = null
- ✅ Guest still seated (no error)

**Test Result:** ✅ PASS / ❌ FAIL

---

## Error Test 6: Unassigned Table (Scenario 2)
**Purpose:** Table with no server assignment

**Step 1:** Delete an assignment (set to inactive)

**Step 2:** Try to seat at that table
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/$GUEST_X/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"tableId": <unassigned_table>}'
```

**Expected Result:**
- ✅ assignedServerId = null
- ✅ Guest still seats (graceful)

**Test Result:** ✅ PASS / ❌ FAIL

---

## Error Test 7: Non-Server Staff (Scenario 1)
**Purpose:** Only SERVER role can be in rotation

**Step:** Try to add HOST to floor
```bash
curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/servers/$HOST_ID/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"onFloor": true}'
```

**Expected Result:**
```json
{
  "success": false,
  "message": "Only staff with role SERVER can be placed on the floor rotation"
}
```

**Test Result:** ✅ PASS / ❌ FAIL

---

# PERFORMANCE TESTING

## Performance Test 1: Large Number of Assignments
**Purpose:** Test with 100+ table assignments

**Step 1:** Create 100 table-server assignments
```bash
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/$i/assign-server \
    -H "Authorization: Bearer $TOKEN_ADMIN" \
    -H "Content-Type: application/json" \
    -d '{"serverId": '$((i % 3 + 1))', "sectionName": "Section '$((i/10))'"}' \
    2>/dev/null
done
```

**Step 2:** Get all assignments
```bash
time curl -X GET http://localhost:8080/api/restaurants/$RESTAURANT_ID/tables/server-assignments \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```

**Expected Result:**
- ✅ Response time < 1 second
- ✅ All 100 assignments returned

**Test Result:** ✅ PASS / ❌ FAIL

---

## Performance Test 2: Seating Under Load
**Purpose:** 50 concurrent seating requests

**Step:** Use Apache Bench or similar
```bash
# Create a file with seating request
echo '{
  "tableId": 1
}' > seat_request.json

ab -n 50 -c 10 -p seat_request.json \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  http://localhost:8080/api/restaurants/$RESTAURANT_ID/waitlist/1/seat
```

**Expected Result:**
- ✅ All requests succeed
- ✅ Average response time < 500ms
- ✅ No errors

**Test Result:** ✅ PASS / ❌ FAIL

---

# FINAL TEST SUMMARY

## Scenario 1 Tests
- [ ] Test 1: Check default setting - ROTATION
- [ ] Test 2: Put servers on floor
- [ ] Test 3: View floor roster
- [ ] Test 4: Create guests
- [ ] Test 5: In-turn seating (auto)
- [ ] Test 6: In-turn seating (server 2)
- [ ] Test 7: Out-of-turn seating (fairness)
- [ ] Test 8: Verify fairness preserved
- [ ] Test 9: Start new shift
- [ ] Test 10: Remove server from floor

**Scenario 1 Result:** ✅ PASS / ❌ FAIL

---

## Scenario 2 Tests
- [ ] Test 1: Switch to SECTION mode
- [ ] Test 2: Verify mode persisted
- [ ] Test 3: Reset servers
- [ ] Test 4: Create table-server assignments
- [ ] Test 5: View all assignments
- [ ] Test 6: View by server
- [ ] Test 7: Create guests
- [ ] Test 8: Seat at Table 1 (Server 1)
- [ ] Test 9: Seat at Table 3 (Server 2)
- [ ] Test 10: Seat at Table 5 (Server 3)
- [ ] Test 11: Seat at reused table
- [ ] Test 12: Update assignment
- [ ] Test 13: Deactivate assignment

**Scenario 2 Result:** ✅ PASS / ❌ FAIL

---

## Integration Tests
- [ ] Integration 1: Switch between scenarios
- [ ] Integration 2: Different restaurants
- [ ] Integration 3: Server notifications

**Integration Result:** ✅ PASS / ❌ FAIL

---

## Error Tests
- [ ] Error 1: No table, no server
- [ ] Error 2: Table not found
- [ ] Error 3: Server not found
- [ ] Error 4: Duplicate assignment
- [ ] Error 5: No servers on floor
- [ ] Error 6: Unassigned table
- [ ] Error 7: Non-server staff

**Error Handling Result:** ✅ PASS / ❌ FAIL

---

## Performance Tests
- [ ] Performance 1: 100+ assignments
- [ ] Performance 2: Concurrent seating

**Performance Result:** ✅ PASS / ❌ FAIL

---

## OVERALL TESTING RESULT

**Scenario 1:** ✅ PASS / ❌ FAIL  
**Scenario 2:** ✅ PASS / ❌ FAIL  
**Integration:** ✅ PASS / ❌ FAIL  
**Error Handling:** ✅ PASS / ❌ FAIL  
**Performance:** ✅ PASS / ❌ FAIL  

**FINAL:** ✅ ALL TESTS PASSED / ❌ SOME TESTS FAILED

**Date Tested:** _______________  
**Tested By:** _______________  
**Notes:** _______________

---

**Document Version:** 1.0  
**Created:** October 16, 2026  
**Purpose:** Complete backend testing guide

