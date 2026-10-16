# Server Rotation Implementation Complete
## Both Scenario 1 & Scenario 2 Now Fully Implemented

**Date:** October 16, 2026  
**Status:** ✅ Ready for Deployment  

---

## 🎯 What's Been Implemented

### Scenario 1: Fair Rotation (ALREADY IMPLEMENTED)
- ✅ Server rotation queue with "Next Up" pointer
- ✅ Fair turn-taking system
- ✅ Out-of-turn pick preserves fairness
- ✅ Manager-initiated shift resets

### Scenario 2: Section-Based Assignment (NOW IMPLEMENTED)
- ✅ Table-to-server permanent assignments
- ✅ Section ownership model
- ✅ Automatic server assignment based on table choice
- ✅ Section labeling support (Window, Bar, Patio, etc.)

---

## 📋 New Files Created

### Entity Classes
- `TableServerAssignment.java` - JPA entity for table-server mappings

### Repositories
- `TableServerAssignmentRepository.java` - Database access layer

### Services
- `ServerSectionAssignmentService.java` - Business logic for Scenario 2

### DTOs (Request/Response)
- `AssignServerToTableRequest.java` - API request model
- `TableServerAssignmentResponse.java` - API response model

### Controllers
- `TableServerAssignmentController.java` - REST endpoints for managing assignments

### Database Migration
- `V10__section_based_table_server_assignment.sql` - Creates new table

### Modified Files
- `AdvancedSettingsPayload.java` - Added `serverAssignmentMode` field
- `RestaurantService.java` - Modified seating logic to check assignment mode

---

## ⚙️ How to Switch Between Scenarios

### Via Admin Settings (Recommended)

**Step 1:** Go to Admin Settings  
→ Advanced Settings  
→ Find "Server Assignment Mode"

**Step 2:** Select One Option:
- **ROTATION** = Scenario 1 (Fair Round-Robin)
- **SECTION** = Scenario 2 (Section-Based)

**Step 3:** Save Settings

The system will **automatically use the selected mode** for all future seating.

### Default Behavior
- On first installation: **ROTATION** (Scenario 1)
- Can be changed anytime from settings without restarting

---

## 🔌 API Endpoints

### New Endpoints for Scenario 2 (Section-Based)

#### 1. Assign Server to a Table
```
POST /api/restaurants/{restaurantId}/tables/{tableId}/assign-server
Authorization: Bearer <JWT>
Role: ADMIN, MANAGER, HOST

Request Body:
{
  "serverId": 5,
  "sectionName": "Window"  // Optional
}

Response:
{
  "success": true,
  "message": "Server assigned to table successfully",
  "data": {
    "id": 1,
    "tableId": 10,
    "tableName": "A1",
    "assignedServerId": 5,
    "serverName": "Alice",
    "sectionName": "Window",
    "isActive": true,
    "createdAt": "2026-10-16T14:30:00",
    "updatedAt": "2026-10-16T14:30:00"
  }
}
```

#### 2. Get All Table-Server Assignments for Restaurant
```
GET /api/restaurants/{restaurantId}/tables/server-assignments
Authorization: Bearer <JWT>
Role: ADMIN, MANAGER, HOST

Response:
{
  "success": true,
  "message": "Table-server assignments retrieved successfully",
  "data": [
    {
      "id": 1,"tableId": 10,"tableName": "A1",
      "assignedServerId": 5,"serverName": "Alice",
      "sectionName": "Window","isActive": true, ...
    },
    {
      "id": 2,"tableId": 11,"tableName": "A2",
      "assignedServerId": 5,"serverName": "Alice",
      "sectionName": "Window","isActive": true, ...
    },
    ...
  ]
}
```

#### 3. Get All Tables for a Specific Server
```
GET /api/restaurants/{restaurantId}/tables/assigned-tables/{serverId}
Authorization: Bearer <JWT>
Role: ADMIN, MANAGER, HOST, SERVER

Response:
{
  "success": true,
  "message": "Tables for server retrieved successfully",
  "data": [
    {"id": 1,"tableId": 10,"tableName": "A1","assignedServerId": 5, ...},
    {"id": 2,"tableId": 11,"tableName": "A2","assignedServerId": 5, ...}
  ]
}
```

#### 4. Deactivate (Remove) an Assignment
```
DELETE /api/restaurants/{restaurantId}/tables/server-assignments/{assignmentId}
Authorization: Bearer <JWT>
Role: ADMIN, MANAGER, HOST

Response:
{
  "success": true,
  "message": "Table-server assignment deactivated successfully",
  "data": null
}
```

---

## 🚀 Getting Started with Scenario 2

### Setup Workflow

**Step 1:** Set Assignment Mode to SECTION
```
Admin Settings → Advanced Settings → Server Assignment Mode = "SECTION"
```

**Step 2:** Assign Servers to Tables
```
For each table, call:
POST /api/restaurants/{id}/tables/{tableId}/assign-server
{
  "serverId": 5,
  "sectionName": "Window"
}
```

**Step 3:** When a Guest Sits
- Host chooses a table → System automatically assigns that table's server
- No rotation logic needed
- Server is notified they've been assigned that table

---

## 📊 Comparison: Both Scenarios Active

| Feature | Scenario 1 (ROTATION) | Scenario 2 (SECTION) |
|---------|---|---|
| **Setting Value** | `serverAssignmentMode = "ROTATION"` | `serverAssignmentMode = "SECTION"` |
| **How Server is Assigned** | Rotation queue + "Next Up" pointer | Table → Server pre-mapping |
| **Host Specifies** | "Who's next up?" or override | "Which table?" |
| **Fairness** | Algorithmic (guaranteed) | Structural (demand-based) |
| **Seating Logic** | `ServerRotationService.assignServerForSeating()` | `ServerSectionAssignmentService.assignServerForSeating()` |
| **Table Setup** | None required | Must assign each table to a server |
| **API Endpoints** | `/servers/floor`, `/servers/shift/start` | `/tables/assign-server`, `/tables/server-assignments` |

---

## 🔄 Code Flow: How It Works

### When a Guest is Seated

1. **API Call:** `POST /api/restaurants/{id}/waitlist/{guestId}/seat`
   ```json
   { "tableId": 10, "serverId": null }  // Optional serverId
   ```

2. **RestaurantService.seatGuest()** Method:
   ```java
   // Get the setting
   String assignmentMode = settings.getAdvancedSettings().getServerAssignmentMode();
   
   // Branch based on mode
   if ("SECTION".equals(assignmentMode)) {
     // Scenario 2: Look up table's assigned server
     assignedServerId = serverSectionAssignmentService.assignServerForSeating(
         restaurantId, 
         tableId
     );
   } else {
     // Scenario 1: Use rotation logic (default)
     assignedServerId = serverRotationService.assignServerForSeating(
         restaurantId, 
         requestedServerId
     );
   }
   
   // Set it on the waitlist
   waitlist.setAssignedServerId(assignedServerId);
   ```

3. **Result:** Server is credited, guest is seated, server is notified

---

## 📋 Database Migration Info

### New Table Created
```
table_server_assignment
├── id (Primary Key)
├── restaurant_id (Foreign Key)
├── table_id (Foreign Key, Unique)
├── assigned_server_id (Staff ID)
├── section_name (Optional label)
├── is_active (Boolean, soft delete)
├── created_at / updated_at (Timestamps)
```

### Run Migration
The migration runs automatically when the application starts:
```
V10__section_based_table_server_assignment.sql
```

---

## 🧪 Testing Scenario 2

### Manual Test Workflow

1. **Enable Scenario 2:**
   ```
   Admin Settings → Advanced Settings → Server Assignment Mode = "SECTION"
   ```

2. **Assign Tables to Servers:**
   ```
   POST /api/restaurants/1/tables/10/assign-server
   {
     "serverId": 5,
     "sectionName": "Window"
   }
   
   POST /api/restaurants/1/tables/11/assign-server
   {
     "serverId": 5,
     "sectionName": "Window"
   }
   
   POST /api/restaurants/1/tables/12/assign-server
   {
     "serverId": 6,
     "sectionName": "Bar"
   }
   ```

3. **Verify Setup:**
   ```
   GET /api/restaurants/1/tables/server-assignments
   → Should show 3 assignments
   
   GET /api/restaurants/1/tables/assigned-tables/5
   → Should show Tables 10 & 11 (Alice's section)
   ```

4. **Seat Guests:**
   ```
   POST /api/restaurants/1/waitlist/100/seat
   { "tableId": 10 }  ← No serverId needed!
   
   → Response: assignedServerId = 5 (Alice automatically)
   ```

5. **Verify Server Got Assigned:**
   ```
   Server Alice logs in → Sees notification: "Assigned Table A1"
   ```

---

## ⚡ Key Features

✅ **Both Scenarios Active** - Switch anytime from settings  
✅ **Automatic Logic Selection** - No code changes needed  
✅ **Full REST API** - All endpoints documented  
✅ **Database Ready** - Migration included  
✅ **Server Notifications** - Works with both scenarios  
✅ **Order Notes** - Works with both scenarios  
✅ **Backward Compatible** - Defaults to Scenario 1  

---

## 📚 Documentation

### For Developers
- See comments in code
- Review DTOs for request/response shapes
- Check service layer for business logic

### For Admins
- Use Admin Settings to switch modes
- Use API endpoints to manage table assignments
- Check server notifications for activity

### For Servers
- In Scenario 1: Wait your turn (no action needed)
- In Scenario 2: Know your tables, get notified of assignments

---

## 🎉 Complete!

The system is now ready to use **BOTH** Scenario 1 and Scenario 2. Clients can:
- ✅ Use Scenario 1 (Fair Rotation) - Current default
- ✅ Switch to Scenario 2 (Section-Based) anytime
- ✅ Manage table-server assignments via API
- ✅ Get server notifications and order notes (both scenarios)

**No additional development needed. Ready for production!**

