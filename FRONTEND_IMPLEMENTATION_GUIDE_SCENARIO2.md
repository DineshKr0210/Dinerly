# Admin Settings UI Guide
## Adding Server Assignment Mode Toggle

**This guide helps frontend developers add the UI for switching between Scenario 1 and Scenario 2.**

---

## Backend Ready

The backend is **100% complete** and ready. These new backend components are available:

### 1. Settings Storage
- **Field:** `AdvancedSettingsPayload.serverAssignmentMode`
- **Type:** String
- **Values:** `"ROTATION"` or `"SECTION"`
- **Default:** `"ROTATION"` (Scenario 1)
- **Endpoint:** `GET/POST /api/settings/advanced` (existing)

### 2. New APIs for Scenario 2
- `POST /api/restaurants/{id}/tables/{tableId}/assign-server` - Assign server to table
- `GET /api/restaurants/{id}/tables/server-assignments` - View all assignments
- `GET /api/restaurants/{id}/tables/assigned-tables/{serverId}` - View server's tables
- `DELETE /api/restaurants/{id}/tables/server-assignments/{assignmentId}` - Remove assignment

---

## Frontend Implementation Steps

### Step 1: Update Admin Settings Page

Add a new section in **Advanced Settings**:

```jsx
<SettingsSection title="Server Assignment">
  <div className="form-group">
    <label>Server Assignment Mode</label>
    <p className="help-text">Choose how servers are assigned to seated guests</p>
    
    <div className="radio-group">
      <label>
        <input 
          type="radio" 
          name="serverAssignmentMode" 
          value="ROTATION"
          checked={settings.serverAssignmentMode === 'ROTATION'}
          onChange={handleChange}
        />
        <strong>ROTATION (Scenario 1 - Fair Turn System)</strong>
        <p className="description">
          Servers take turns in a queue. "Next Up" server gets guests unless 
          a specific server is requested manually.
        </p>
      </label>
      
      <label>
        <input 
          type="radio" 
          name="serverAssignmentMode" 
          value="SECTION"
          checked={settings.serverAssignmentMode === 'SECTION'}
          onChange={handleChange}
        />
        <strong>SECTION (Scenario 2 - Section-Based)</strong>
        <p className="description">
          Each table is assigned to a server. Guest sits at a table → 
          that server is automatically assigned.
        </p>
      </label>
    </div>
  </div>
</SettingsSection>
```

### Step 2: Add New "Table Assignments" Tab

If using Scenario 2, add a new admin section:

**Navigation:** Admin → Tables → Server Assignments

```jsx
<Table>
  <TableHead>
    <tr>
      <th>Table</th>
      <th>Assigned Server</th>
      <th>Section</th>
      <th>Status</th>
      <th>Actions</th>
    </tr>
  </TableHead>
  <TableBody>
    {assignments.map(a => (
      <tr key={a.id}>
        <td>{a.tableName}</td>
        <td>{a.serverName}</td>
        <td>{a.sectionName || '-'}</td>
        <td>{a.isActive ? 'Active' : 'Inactive'}</td>
        <td>
          <button onClick={() => editAssignment(a)}>Edit</button>
          <button onClick={() => deleteAssignment(a.id)}>Remove</button>
        </td>
      </tr>
    ))}
  </TableBody>
</Table>
```

### Step 3: Add Assignment Modal/Form

For editing table-server assignments:

```jsx
<Modal title="Assign Server to Table">
  <FormGroup>
    <Label>Table</Label>
    <Select value={selectedTable} onChange={e => setSelectedTable(e.target.value)}>
      {tables.map(t => <option key={t.id}>{t.tableName}</option>)}
    </Select>
  </FormGroup>
  
  <FormGroup>
    <Label>Server</Label>
    <Select value={selectedServer} onChange={e => setSelectedServer(e.target.value)}>
      {servers.map(s => <option key={s.id}>{s.name}</option>)}
    </Select>
  </FormGroup>
  
  <FormGroup>
    <Label>Section Name (Optional)</Label>
    <Input 
      type="text" 
      placeholder="e.g., Window, Bar, Patio"
      value={sectionName}
      onChange={e => setSectionName(e.target.value)}
    />
  </FormGroup>
  
  <Button onClick={handleAssign}>Assign</Button>
</Modal>
```

### Step 4: API Calls

**Save Setting:**
```javascript
async function saveSetting(mode) {
  const response = await fetch('/api/settings/advanced', {
    method: 'POST',
    headers: { 
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      serverAssignmentMode: mode
    })
  });
  return response.json();
}
```

**Assign Server to Table:**
```javascript
async function assignServerToTable(restaurantId, tableId, serverId, sectionName) {
  const response = await fetch(
    `/api/restaurants/${restaurantId}/tables/${tableId}/assign-server`,
    {
      method: 'POST',
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        serverId,
        sectionName
      })
    }
  );
  return response.json();
}
```

**Get All Assignments:**
```javascript
async function getAssignments(restaurantId) {
  const response = await fetch(
    `/api/restaurants/${restaurantId}/tables/server-assignments`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  return response.json();
}
```

---

## UI Flow for Each Scenario

### Scenario 1 (ROTATION) UI
- ✅ Show "Floor Roster" with "Next Up" indicator
- ✅ Show seating dialog: "Next Up: Alice" (with override option)
- ✅ Show "Start Shift" button
- ✅ **Hide** table assignment section

### Scenario 2 (SECTION) UI
- ✅ Show "Table Assignments" section
- ✅ Show seating dialog: "Choose table → auto-assign server"
- ✅ **Hide** rotation/floor roster section
- ✅ **Hide** "Start Shift" button
- ✅ Show "Edit Assignments" button

---

## Conditional Rendering

### Pseudocode
```javascript
const assignmentMode = settings.serverAssignmentMode;

if (assignmentMode === 'ROTATION') {
  // Show Scenario 1 UI
  return <RotationFloorRoster />;
} else if (assignmentMode === 'SECTION') {
  // Show Scenario 2 UI
  return <TableAssignmentGrid />;
}
```

---

## Design Recommendations

### For Settings Page
- Add info icons explaining both modes
- Consider before/after diagrams
- Include migration guide if switching modes

### For Table Assignments
- Bulk assignment feature ("Assign all tables to one server")
- Section color coding (visual grouping)
- Drag-and-drop reassignment
- Template-based setup ("Copy assignments from last week")

### For Seating Dialog
- Show current table's assigned server (Scenario 2)
- Show fairness indicator (Scenario 1)
- Clear instructions on how each mode works

---

## Error Handling

Handle these cases on the frontend:

1. **No servers on floor** (Scenario 1): Show error → "Add servers first"
2. **Table not assigned** (Scenario 2): Show error → "Assign a server to this table"
3. **Missing Mode Setting**: Default to Scenario 1
4. **Assignment Conflict**: Show warning → "This table is already assigned"

---

## Testing Checklist

- [ ] Switch mode from Scenario 1 → 2 (settings saves correctly)
- [ ] Switch mode from Scenario 2 → 1 (old settings preserved)
- [ ] Assign server to table (API returns success)
- [ ] View all assignments (table shows correct data)
- [ ] Seat guest in Scenario 1 (rotation works)
- [ ] Seat guest in Scenario 2 (correct server assigned)
- [ ] Change mode mid-seating (handles gracefully)
- [ ] Permission checks (only ADMIN/MANAGER can edit settings)

---

## Backend Validation

These are handled by the backend:

✅ Server exists and belongs to restaurant  
✅ Table exists and belongs to restaurant  
✅ Unique table assignment (each table assigned to one server)  
✅ Soft delete (deactivate doesn't remove data)  
✅ No harm if mode is changed (both data structures coexist)  

---

## Questions?

- **Backend API Details:** See `SCENARIO_2_IMPLEMENTATION_COMPLETE.md`
- **How Seating Changes:** See `SERVER_ROTATION_COMPARISON.md`
- **Data Model:** Check entity classes in `/entity/`


