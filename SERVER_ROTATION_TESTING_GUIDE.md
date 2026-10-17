# Server Rotation — How It Works & How To Test It

This document explains the "Server Rotation Logic" feature end-to-end: both
assignment scenarios, the setting that switches between them, every API involved,
exact request/response shapes, and a step-by-step functional test script you can run
with `curl` (or import into Postman/Swagger).

> **Updated in this revision:** added a per-server workload cap (`maxTables`) with a
> live `currentTableCount`/`atCapacity` flag on the floor roster, a way to actually
> finish a seated party (`POST /waitlist/{id}/complete` — previously a `SEATED` entry
> had no way out of that status at all), and the floor roster response is now a
> wrapped object that also reports which mode is active plus each server's assigned
> table names in SECTION mode.
>
> **Also fixed while testing this:** three untracked database CHECK constraints
> (`staff_role_check`, `waitlist_status_check`/`waitlist_last_active_status_check`,
> `users_role_check`) predated the `SERVER` role and `COMPLETED` status being added to
> their Java enums, and silently rejected both — meaning the *entire* server rotation
> feature, and logging in as a server at all, was broken at the database level until
> `V12__server_capacity_and_completed_status.sql` fixed them. If you're testing against
> an older database, make sure this migration has actually run before anything else here.

---

## 1. The two scenarios, and the setting that picks one

A restaurant runs in exactly one of two modes at a time, stored in its Advanced
Settings as `serverAssignmentMode`:

### Scenario 1 — `"ROTATION"` (default): Fair Turn System
- Every server **on the floor** sits in an ordered rotation list.
- Exactly **one** on-floor server is marked **Next Up**.
- When a party is seated:
  - No server requested → the **Next Up** server gets them, `seatedCount +1`, and the
    pointer **advances** to the next server in line.
  - A **specific** server requested (guest asked for someone, or host picked someone
    for load-balancing) → that server gets them, `seatedCount +1`, but the Next-Up
    pointer **does not move**. The skipped server stays first in line for the next
    seating. The pointer never silently skips anyone.
- A **manager/admin** can explicitly **"Start a new shift"**, resetting every on-floor
  server's `seatedCount` to 0. This is manual only — there is no automatic daily reset.

### Scenario 2 — `"SECTION"`: Section Owner System
- Each table is permanently (until reassigned) owned by one server via a
  **table → server** mapping, independent of any queue or pointer.
- When a party is seated at a table, whoever owns that table's assignment serves them
  — automatically, every time, regardless of turn order.
- If a server calls out, a manager manually reassigns their tables; there is no
  automatic failover.

### Switching modes
```
GET  /api/admin/settings/{restaurantId}/advanced
PUT  /api/admin/settings/{restaurantId}/advanced
```
`PUT` body (all fields; `serverAssignmentMode` is the one that matters here):
```json
{
  "darkMode": false,
  "desktopNotifications": false,
  "keepSignedIn": true,
  "language": "en",
  "timezone": "Canada/Central",
  "serverAssignmentMode": "SECTION"
}
```
`serverAssignmentMode` must be exactly `"ROTATION"` or `"SECTION"` (case-insensitive,
normalized to uppercase) — anything else returns `400 Bad Request`. Omit the field
entirely to leave the current mode unchanged.

`GET` returns the current value in the same shape, so **always check this first**
before running a seating test — a leftover `SECTION` mode from a previous test run
will make Scenario 1 steps fail in confusing ways, and vice versa.

---

## 2. Data model (reference)

| Entity | Fields | Purpose |
|---|---|---|
| `Staff` | role can be `SERVER` | Identifies floor servers |
| `Table` | `currentServerId` | Who is currently serving this table |
| `Waitlist` | `tableId`, `assignedServerId`, `orderNotes` | Which table/server a seated party is linked to |
| `ServerFloorStatus` | `staffId`, `onFloor`, `seatedCount`, `rotationOrder`, `isNextUp` | Rotation state, one row per server (Scenario 1 only) |
| `TableServerAssignment` | `tableId`, `assignedServerId`, `sectionName`, `isActive` | Static table→server ownership (Scenario 2 only) |
| `ServerNotification` | `staffId`, `waitlistId`, `tableId`, `message`, `isRead` | "You've been assigned Table X" feed |
| `RestaurantSettings.advancedSettings.serverAssignmentMode` | `"ROTATION"` \| `"SECTION"` | Which engine seating uses |

Migrations: `V8__server_rotation_and_table_assignment.sql`,
`V9__server_order_notes.sql`, `V10__section_based_table_server_assignment.sql`,
`V12__server_capacity_and_completed_status.sql` (adds `max_tables`, the `COMPLETED`
waitlist status, and fixes the three stale CHECK constraints mentioned above).

---

## 3. All APIs involved

All endpoints require `Authorization: Bearer <JWT>` (from `/api/auth/login`) unless
noted. Base URL below assumes `http://localhost:8080`.

### 3.1 Setup/auth

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 1 | `POST /api/auth/login` | public | Get a JWT for ADMIN/MANAGER/HOST/SERVER |
| 2 | `POST /api/admin/staff` | ADMIN | Create a staff member (use `"role": "SERVER"`) |
| 3 | `POST /api/admin/staff/set-password` | public (token) | Server accepts invite & sets password → creates their login |
| 4 | `POST /api/restaurants/{restaurantId}/tables` | ADMIN/MANAGER/HOST | Create floor-plan tables |
| 5 | `GET  /api/restaurants/{restaurantId}/tables` | ADMIN/MANAGER/HOST | List tables (includes `currentServerId`) |
| 6 | `POST /api/restaurants/{restaurantId}/waitlist` | ADMIN/MANAGER/HOST | Add a guest to the waitlist |

### 3.2 Mode switch (settings)

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 7 | `GET /api/admin/settings/{restaurantId}/advanced` | ADMIN (own location) | Read current `serverAssignmentMode` |
| 8 | `PUT /api/admin/settings/{restaurantId}/advanced` | ADMIN (own location) | Set `serverAssignmentMode` to `ROTATION` or `SECTION` |

### 3.3 Scenario 1 — rotation / floor management

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 9 | `GET  /api/restaurants/{restaurantId}/servers/floor` | ADMIN/MANAGER/HOST | View the roster — wrapped with `serverAssignmentMode`, each server's live `currentTableCount`/`maxTables`/`atCapacity`, and (SECTION mode) `assignedTableNames` |
| 10 | `POST /api/restaurants/{restaurantId}/servers/{staffId}/floor` | ADMIN/MANAGER/HOST | Put a server on/off the floor, and/or set their capacity. Body: `{ "onFloor": true, "maxTables": 3 }` (`maxTables` optional: omit to leave unchanged, `0` resets to unlimited) |
| 11 | `POST /api/restaurants/{restaurantId}/servers/shift/start` | **ADMIN/MANAGER only** | Start a new shift — resets seatedCount for everyone on the floor |

### 3.4 Scenario 2 — section assignment

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 12 | `POST   /api/restaurants/{restaurantId}/tables/{tableId}/assign-server` | ADMIN/MANAGER/HOST | Assign a table to a server's section. Body: `{ "tableId": 5, "serverId": 12, "sectionName": "Window" }` |
| 13 | `GET    /api/restaurants/{restaurantId}/tables/server-assignments` | ADMIN/MANAGER/HOST | List every table→server assignment |
| 14 | `GET    /api/restaurants/{restaurantId}/tables/assigned-tables/{serverId}` | ADMIN/MANAGER/HOST/SERVER | List tables owned by one server |
| 15 | `DELETE /api/restaurants/{restaurantId}/tables/server-assignments/{assignmentId}` | ADMIN/MANAGER/HOST | Deactivate an assignment (e.g. server called out) |

### 3.5 Seating (both scenarios go through one of these two)

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 16 | `POST /api/restaurants/{restaurantId}/waitlist/{id}/seat` | ADMIN/MANAGER/HOST | Seat a specific guest from the **queue**. Body (all optional): `{ "tableName": "B2", "tableId": 5, "serverId": 12 }`. `serverId` is only honored in ROTATION mode (explicit/out-of-turn pick); in SECTION mode the table's owner is always used. |
| 17 | `POST /api/restaurants/{restaurantId}/tables/{tableId}/seat-next-party` | ADMIN/MANAGER/HOST | Seat the **front of the queue** directly from a table's own detail view. Body (optional): `{ "serverId": 12 }`. Table must be `OPEN`. Internally calls the exact same seating logic as #16, so the Next-Up pointer / section lookup behaves identically regardless of which of these two you use. |
| 18 | `POST /api/restaurants/{restaurantId}/waitlist/{id}/complete` | ADMIN/MANAGER/HOST | **New.** Marks a seated party as finished: `Waitlist.status → COMPLETED`, and if a real table was linked, frees it (`status → NEEDS_CLEANING`, `currentServerId` cleared) in the same call. This is what makes a server's `currentTableCount` go back down. |

### 3.6 Server's own screen

| # | Method & Path | Role | Purpose |
|---|---|---|---|
| 19 | `GET  /api/restaurants/{restaurantId}/servers/me/tables` | SERVER (or ADMIN/MANAGER/HOST) | "My Tables" — every currently-seated party assigned to me |
| 20 | `POST /api/restaurants/{restaurantId}/servers/me/tables/{waitlistId}/notes` | **SERVER only** | Save order notes for one of my seated tables |
| 21 | `GET  /api/restaurants/{restaurantId}/servers/me/notifications?page=0&size=20` | SERVER | My notification feed |
| 22 | `GET  /api/restaurants/{restaurantId}/servers/me/notifications/unread-count` | SERVER | Badge count |
| 23 | `POST /api/restaurants/{restaurantId}/servers/me/notifications/{notificationId}/read` | SERVER | Mark a notification read |

> Endpoints 19–23 resolve "me" from the JWT (via the linked `Staff` record) — the
> server does not pass their own staffId.

All responses use the standard envelope: `{ "success": true, "message": "...", "data": {...} }`.

---

## 4. Functional test script — Scenario 1 (ROTATION)

Replace `TOKEN_ADMIN` / `TOKEN_SERVER_1` / `TOKEN_SERVER_2` with real JWTs after
login, and `RID` with your restaurant id.

### Step 0 — Confirm the mode, then log in
```bash
curl -s http://localhost:8080/api/admin/settings/RID/advanced -H "Authorization: Bearer $TOKEN_ADMIN"
# data.serverAssignmentMode should be "ROTATION". If it's "SECTION", switch it first:
curl -s -X PUT http://localhost:8080/api/admin/settings/RID/advanced \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"darkMode":false,"desktopNotifications":false,"keepSignedIn":true,"language":"en","timezone":"Canada/Central","serverAssignmentMode":"ROTATION"}'
```

### Step 1 — Create two SERVER staff members
```bash
curl -s -X POST http://localhost:8080/api/admin/staff \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"name":"Alice","role":"SERVER","locationId":RID,"email":"alice@test.com"}'

curl -s -X POST http://localhost:8080/api/admin/staff \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"name":"Bob","role":"SERVER","locationId":RID,"email":"bob@test.com"}'
# => note the returned staffId for each (data.id). The staff row is usable
# immediately for floor/section assignment, even before they set a password.
```

### Step 2 — Create 4 tables
```bash
for n in A1 A2 B1 B2; do
curl -s -X POST http://localhost:8080/api/restaurants/RID/tables \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d "{\"tableNumber\":\"$n\",\"capacity\":4}"
done
```

### Step 3 — Put both servers on the floor
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/<ALICE_STAFF_ID>/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"onFloor": true}'

curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/<BOB_STAFF_ID>/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"onFloor": true}'
```

### Step 4 — Check the roster
```bash
curl -s http://localhost:8080/api/restaurants/RID/servers/floor -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** Alice first (added first) → `onFloor:true, seatedCount:0, nextUp:true`.
Bob → `onFloor:true, seatedCount:0, nextUp:false`.

### Step 5 — Add 4 guests to the waitlist
```bash
for i in 1 2 3 4; do
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d "{\"name\":\"Guest $i\",\"phone\":\"555000000$i\",\"partySize\":2,\"position\":$i}"
done
# note each returned data.id as WID1..WID4
```

### Step 6 — Seat Guest 1 with NO server specified (auto Next Up)
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID1/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <A1_TABLE_ID>}'
```
**Expect:** `data.assignedServerId` == Alice's staffId. Re-check the roster → Alice
`seatedCount:1, nextUp:false`; Bob `nextUp:true`.

### Step 7 — Seat Guest 2 OUT OF TURN (guest explicitly requests Alice)
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID2/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <A2_TABLE_ID>, "serverId": <ALICE_STAFF_ID>}'
```
**Expect:** `data.assignedServerId` == Alice, Alice `seatedCount:2`.
**Critical check:** re-check the roster → **Bob must still be `nextUp:true`** — the
pointer was not advanced because this was an out-of-turn pick.

### Step 8 — Seat Guest 3 with NO server specified again
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID3/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <B1_TABLE_ID>}'
```
**Expect:** `data.assignedServerId` == Bob (he was still Next Up from step 6, proving
he wasn't skipped). Bob `seatedCount:1`. Pointer now advances back to Alice.

### Step 9 — Seat Guest 4 via "seat next party" from the table's own detail view
This is the **second entry point** — instead of picking a waitlist id and a table
separately, you act from an `OPEN` table and let it pull the front of the queue.
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/tables/<B2_TABLE_ID>/seat-next-party \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{}'
```
**Expect:** Guest 4 (front of the queue) gets seated at B2, `data.assignedServerId` ==
Alice (she's Next Up again after step 8) — identical pointer behavior to the
queue-based endpoint, since both call the same underlying logic.
**Negative check:** call this again on a table that's already `OCCUPIED` → expect
`400 Bad Request` ("Table is not open"). Call it when the queue is empty → expect
`400 Bad Request` ("No guests waiting to be seated").

### Step 10 — Merged-table group = ONE seating event
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/tables/merge \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <TABLE_X>, "mergedTableId": <TABLE_Y>}'

curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID5/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <TABLE_X>}'

curl -s http://localhost:8080/api/restaurants/RID/tables -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** Both `TABLE_X` and `TABLE_Y` show the **same** `currentServerId` and
`status:OCCUPIED` — only +1 to that one server's `seatedCount`, not +2.

### Step 11 — Set Alice's capacity and watch `atCapacity` flip
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/<ALICE_STAFF_ID>/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"onFloor": true, "maxTables": 2}'

curl -s http://localhost:8080/api/restaurants/RID/servers/floor -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** Alice's row shows `maxTables:2` and `currentTableCount` equal to however
many of her parties (from steps 6/7/10) are still `SEATED`. If that count is already
`>= 2`, `atCapacity:true`. This is **visibility only** — it does not block a new
seating from landing on her; the host decides whether to route around it.

### Step 12 — Finish one of Alice's parties and watch the count drop
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID1/complete \
  -H "Authorization: Bearer $TOKEN_ADMIN"

curl -s http://localhost:8080/api/restaurants/RID/tables -H "Authorization: Bearer $TOKEN_ADMIN"
curl -s http://localhost:8080/api/restaurants/RID/servers/floor -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** the waitlist response shows `status:"COMPLETED"`. A1 (Guest 1's table)
shows `status:"NEEDS_CLEANING"` and `currentServerId:null` — cleared automatically in
the same call. Alice's `currentTableCount` drops by 1 and `atCapacity` goes back to
`false` once she's under the cap again — this is what "picking up another table" looks
like: free capacity by completing, then she's eligible for the next seating same as before.
**Negative checks:** completing the same waitlist id again → `400` ("Only a seated
party can be marked as completed"). Completing a `WAITING`/`CANCELLED` entry → same error.

### Step 13 — Manager starts a new shift (manual reset only)
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/shift/start \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** Alice & Bob both `seatedCount:0`; rotation restarts from the first server
in `rotationOrder`, marked `nextUp:true`.
**Also verify:** without calling this, `seatedCount` never resets on its own.

### Step 14 — Removing a server from the floor mid-shift
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/servers/<BOB_STAFF_ID>/floor \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"onFloor": false}'

curl -s http://localhost:8080/api/restaurants/RID/servers/floor -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** Bob `onFloor:false`. If Bob was `nextUp`, the pointer moves to Alice. Seat
a new guest with no `serverId` → goes to Alice (Bob is skipped because he clocked out,
not because of unfair rotation).

### Step 15 — No servers on the floor
Remove Alice too, then seat a guest with no `serverId`.
**Expect:** `data.assignedServerId` is `null` — seating still succeeds without the
rotation feature active.

### Step 16 — Permission checks
- `POST /servers/shift/start` with a **HOST** token → expect `403` (ADMIN/MANAGER only).
- `GET /servers/me/tables` with Bob's token → only Bob's tables, never Alice's.
- `POST /servers/me/tables/{waitlistId}/notes` with an ADMIN/MANAGER/HOST token →
  expect `403` (SERVER-only).
- Any `/servers/...` endpoint with no `Authorization` header → expect `401`/`403`.

---

## 5. Functional test script — Scenario 2 (SECTION)

Reuses Alice/Bob/tables from section 4. Run this **after** switching the mode.

### Step 1 — Switch the restaurant to SECTION mode
```bash
curl -s -X PUT http://localhost:8080/api/admin/settings/RID/advanced \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"darkMode":false,"desktopNotifications":false,"keepSignedIn":true,"language":"en","timezone":"Canada/Central","serverAssignmentMode":"SECTION"}'
```
**Expect:** `data.serverAssignmentMode == "SECTION"`. Confirm with a `GET` too.

### Step 2 — Assign tables to each server's section
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/tables/<A1_TABLE_ID>/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <A1_TABLE_ID>, "serverId": <ALICE_STAFF_ID>, "sectionName": "Window"}'

curl -s -X POST http://localhost:8080/api/restaurants/RID/tables/<B1_TABLE_ID>/assign-server \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <B1_TABLE_ID>, "serverId": <BOB_STAFF_ID>, "sectionName": "Bar"}'
```
**Expect:** both return `200` with the new assignment (`tableName`, `serverName`,
`sectionName`, `isActive:true`).

### Step 3 — List all assignments
```bash
curl -s http://localhost:8080/api/restaurants/RID/tables/server-assignments -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** both assignments listed.

### Step 4 — Seat a guest at Alice's table — no rotation involved
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID6/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <A1_TABLE_ID>}'
```
**Expect:** `data.assignedServerId` == Alice, **even if Bob was "Next Up"** in the old
ROTATION roster — section mode ignores the rotation pointer entirely. Any `serverId`
passed in the body is ignored in this mode (the table's owner always wins).

### Step 5 — Seat a guest at Bob's table via "seat next party"
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/tables/<B1_TABLE_ID>/seat-next-party \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" -d '{}'
```
**Expect:** `data.assignedServerId` == Bob — same section lookup, same result whether
seated via the queue or via this endpoint.

### Step 6 — Seat at a table with no assignment
```bash
curl -s -X POST http://localhost:8080/api/restaurants/RID/waitlist/WID7/seat \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"tableId": <A2_TABLE_ID>}'
```
**Expect:** `data.assignedServerId` is `null` (A2 was never assigned to anyone) — no
error, seating still succeeds.

### Step 7 — Server calls out: deactivate their assignment
```bash
curl -s -X DELETE http://localhost:8080/api/restaurants/RID/tables/server-assignments/<ASSIGNMENT_ID> \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** `200`. Re-run Step 4 at the same table → `assignedServerId` now `null`
until a manager manually reassigns it (no automatic failover, by design).

### Step 8 — Switch back to ROTATION and confirm state survived
```bash
curl -s -X PUT http://localhost:8080/api/admin/settings/RID/advanced \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"darkMode":false,"desktopNotifications":false,"keepSignedIn":true,"language":"en","timezone":"Canada/Central","serverAssignmentMode":"ROTATION"}'

curl -s http://localhost:8080/api/restaurants/RID/servers/floor -H "Authorization: Bearer $TOKEN_ADMIN"
```
**Expect:** the rotation roster/pointer from section 4 is unchanged — switching modes
only changes which engine seating *uses*, it doesn't reset or touch the other
engine's state. Section assignments made in step 2 also still exist (list them again
via Step 3's call) — they're just not consulted while in ROTATION mode.

---

## 6. Edge cases to verify

| Scenario | Expected behavior |
|---|---|
| `serverAssignmentMode` set to anything other than `ROTATION`/`SECTION` | `400 Bad Request` — "serverAssignmentMode must be ROTATION or SECTION" |
| `serverId` passed in ROTATION mode but that server is NOT on the floor | `400 Bad Request` — "Selected server is not currently on the floor" |
| `staffId` passed to `/floor` belongs to a different restaurant | `400 Bad Request` |
| `staffId` passed to `/floor` is not role `SERVER` (e.g. a HOST) | `400 Bad Request` — "Only staff with role SERVER can be placed on the floor rotation" |
| Only 1 server on the floor (ROTATION) | Always Next Up; pointer "advances" to itself |
| `seat-next-party` on a table that's `OCCUPIED`, `RESERVED`, or `NEEDS_CLEANING` | `400 Bad Request` — "Table is not open" |
| `seat-next-party` with nobody `WAITING` in the queue | `400 Bad Request` — "No guests waiting to be seated" |
| Table status manually reset via `POST /tables/{id}/status` | `currentServerId` on that table is cleared automatically |
| Guest seated without `tableId` | Still works — only `tableName` (free text) stored; `assignedServerId` still comes from ROTATION if applicable, or `null` in SECTION mode (no table to look up) |
| Order notes on a table not assigned to the caller | `400 Bad Request` |
| Order notes on a party that isn't currently `SEATED` | `400 Bad Request` |
| `maxTables` set but a server is already over it before you lower it | `atCapacity:true` immediately on the next roster read — no action is blocked, it's informational |
| `maxTables: 0` sent to `/floor` | Explicitly resets that server back to unlimited capacity |
| `/waitlist/{id}/complete` called on a `WAITING`/`NOTIFIED`/`CANCELLED`/already-`COMPLETED` entry | `400 Bad Request` — "Only a seated party can be marked as completed" |
| `/waitlist/{id}/complete` called on a party seated without a real `tableId` (free-text `tableName` only) | Still succeeds — just nothing to free on the Table side |

---

## 7. Quick reference — request/response shapes

**`PUT /admin/settings/{restaurantId}/advanced`**
```json
{ "darkMode": false, "desktopNotifications": false, "keepSignedIn": true,
  "language": "en", "timezone": "Canada/Central", "serverAssignmentMode": "ROTATION" }
```

**`POST /servers/{staffId}/floor`**
```json
{ "onFloor": true, "maxTables": 3 }
```
(`maxTables` optional — omit to leave unchanged, `0` resets to unlimited)

**`GET /servers/floor` → `data`** (now a wrapped object, not a bare array)
```json
{
  "serverAssignmentMode": "ROTATION",
  "servers": [
    { "staffId": 12, "name": "Alice", "onFloor": true, "seatedCount": 2, "rotationOrder": 1,
      "nextUp": false, "currentTableCount": 2, "maxTables": 2, "atCapacity": true,
      "assignedTableNames": null },
    { "staffId": 13, "name": "Bob", "onFloor": true, "seatedCount": 1, "rotationOrder": 2,
      "nextUp": true, "currentTableCount": 1, "maxTables": null, "atCapacity": false,
      "assignedTableNames": null }
  ]
}
```
`assignedTableNames` is only populated when `serverAssignmentMode == "SECTION"` (e.g.
`["A1", "A2"]`); it's `null` in ROTATION mode since there's no static ownership to show.

**`POST /waitlist/{id}/complete`** (no body)
→ `data` is the same `WaitlistResponse` shape as `/seat`, with `status:"COMPLETED"`.

**`POST /tables/{tableId}/assign-server`**
```json
{ "tableId": 5, "serverId": 12, "sectionName": "Window" }
```

**`POST /waitlist/{id}/seat`**
```json
{ "tableName": "B2", "tableId": 5, "serverId": 12 }
```
(all fields optional — omit `serverId` for auto Next-Up in ROTATION, ignored entirely in SECTION)

**`POST /tables/{tableId}/seat-next-party`**
```json
{ "serverId": 12 }
```
(body itself is optional — omit or send `{}` to let the mode's default assignment apply)

**`GET /servers/me/tables` → `data`**
```json
[
  { "waitlistId": 101, "tableId": 5, "tableName": "B2", "guestName": "Guest 1",
    "partySize": 2, "seatedAt": "2026-10-10T19:05:00", "orderNotes": null }
]
```
