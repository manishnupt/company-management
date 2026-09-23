# HRMS Company Policy Module — Phase 1 Contract & Flow

## 1. Purpose

This document is the implementation contract for the HRMS Company Policy module discussed in this chat.

Use it as a checklist to verify whether the backend implementation matches the agreed design.

The system is multi-tenant already, with each tenant using its own database. This document therefore focuses only on the policy-domain design.

---

# 2. Core Business Rules

## 2.1 Employee → Group → Policy

The agreed hierarchy is:

Employee → Group → Policy

- Each employee belongs to exactly one group.
- Each group has exactly one currently assigned policy.
- The same policy can be assigned to multiple groups.
- Policy assignment is at group level, not directly at employee level.
- Historical group assignment is NOT required at this stage.

Conceptually:

```text
Employee
   |
   v
Group
   |
   v
Policy
```

---

# 3. Policy Versioning

We decided to use a SINGLE `policies` table rather than separate `policy` and `policy_version` tables.

Each new policy version is a new row with:

- a new policy ID
- an incremented version number
- its own configuration
- its own lifecycle/status

Example:

```text
Policy A
Version 1
ID = P1
Status = ACTIVE

        |
        | create new version
        v

Policy A
Version 2
ID = P2
Status = DRAFT
```

The old version remains unchanged.

## 3.1 Important versioning rules

- Only `DRAFT` policies can be modified.
- Only `ACTIVE` policies can be assigned to groups.
- Publishing a policy changes it from `DRAFT` → `ACTIVE`.
- Once ACTIVE, policy configuration should be locked.
- Existing employee balances must NOT be rewritten when a policy changes.
- Future leave/WFH disbursal should use the policy applicable to the employee's group at that time.
- Historical policy configuration must remain available for audit.

---

# 4. Policy Entity

Expected structure:

```java
@Entity
@Table(
    name = "policies",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_name_version",
            columnNames = {"name", "version"}
        )
    }
)
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;
}
```

Policy status:

```java
public enum PolicyStatus {
    DRAFT,
    ACTIVE,
    INACTIVE,
    EXPIRED
}
```

---

# 5. Phase 1 Scope

Phase 1 includes:

1. Work Schedule
2. Attendance Rules
3. Leave Types
4. WFH Types
5. Employee Lifecycle
6. Miscellaneous
7. Policy management
8. Group → Policy assignment

Phase 1 explicitly DOES NOT include:

- Overtime
- Holiday Calendar

These are future/Phase 2 items.

---

# 6. Work Schedule

## 6.1 Relationship

One policy has exactly one work schedule.

```text
Policy
  |
  └── WorkSchedule
```

## 6.2 Entity

```java
@Entity
@Table(
    name = "policy_work_schedules",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_work_schedule",
            columnNames = "policy_id"
        )
    }
)
public class PolicyWorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "policy_id",
        nullable = false,
        unique = true
    )
    private Policy policy;

    @Column(nullable = false)
    private LocalTime checkInTime;

    @Column(nullable = false)
    private LocalTime checkOutTime;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal minimumWorkingHours;

    @Column(
        name = "working_days",
        columnDefinition = "varchar[]",
        nullable = false
    )
    private String[] workingDays;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Important

`working_days` is a PostgreSQL array.

Do NOT create a separate `PolicyWorkingDay` table.

Example:

```text
["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"]
```

## 6.3 API

```http
PUT /policies/{policyId}/work-schedule
GET /policies/{policyId}/work-schedule
```

Request:

```json
{
  "checkInTime": "09:00",
  "checkOutTime": "18:00",
  "minimumWorkingHours": 8.0,
  "workingDays": [
    "MONDAY",
    "TUESDAY",
    "WEDNESDAY",
    "THURSDAY",
    "FRIDAY"
  ]
}
```

Rules:

- Policy must exist.
- Policy must be DRAFT for modification.
- Working days cannot be empty.
- Minimum working hours must be > 0.
- Check-in/check-out should be validated logically.

---

# 7. Attendance Rules

One attendance configuration per policy.

```text
Policy
  |
  └── AttendanceRules
```

## 7.1 Entity

```java
@Entity
@Table(
    name = "policy_attendance_rules",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_attendance_rule",
            columnNames = "policy_id"
        )
    }
)
public class PolicyAttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "policy_id",
        nullable = false,
        unique = true
    )
    private Policy policy;

    private Integer lateMarkThresholdMinutes;

    private Integer consecutiveLateDaysAllowed;

    private BigDecimal autoMarkAbsentAfterHours;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 7.2 API

```http
PUT /policies/{policyId}/attendance
GET /policies/{policyId}/attendance
```

Request:

```json
{
  "lateMarkThresholdMinutes": 15,
  "consecutiveLateDaysAllowed": 3,
  "autoMarkAbsentAfterHours": 4
}
```

Rules:

- Policy must exist.
- Policy must be DRAFT for modification.
- Numeric values must be valid/non-negative as appropriate.

---

# 8. Leave Types

Leave types are POLICY-OWNED.

There is NO global leave-type master catalog in this design.

There is also NO separate `PolicyLeaveRule` table.

The existing `LeaveType` entity gets a `policy_id`.

## 8.1 Entity

```java
@Entity
@Table(
    name = "leave_types",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_leave_type_policy_name",
            columnNames = {"policy_id", "name"}
        )
    }
)
public class LeaveType {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    private String name;

    private int totalDays;

    private boolean carryForward;

    @Enumerated(EnumType.STRING)
    private DisbursalFrequency disbursalFrequency;

    private String description;
}
```

## 8.2 Important business rule

Different policies can have leave types with the same name.

Example:

```text
Policy A
  Casual Leave = 12 days

Policy B
  Casual Leave = 8 days
```

But within the same policy:

```text
Policy A
  Casual Leave
  Casual Leave   <-- NOT allowed
```

Therefore:

```text
UNIQUE(policy_id, name)
```

## 8.3 Disbursal configuration

There is NO separate leave-disbursal-schedule table.

The LeaveType itself contains:

- totalDays
- carryForward
- disbursalFrequency

These are the leave policy rules.

## 8.4 APIs

```http
POST /policies/{policyId}/leave-types
GET  /policies/{policyId}/leave-types
```

Request:

```json
{
  "name": "Casual Leave",
  "totalDays": 12,
  "carryForward": true,
  "disbursalFrequency": "MONTHLY",
  "description": "Casual leave entitlement"
}
```

`policyId` comes from the URL/path variable, NOT from the request body.

Existing individual APIs can remain:

```http
GET    /leave-types/{id}
PUT    /leave-types/{id}
DELETE /leave-types/{id}
```

However, modification/deletion should respect policy status and should not allow changes to ACTIVE policy configuration.

---

# 9. WFH Types

WFH types are also POLICY-OWNED.

There is NO global WFH type master catalog.

There is NO separate `PolicyWfhRule` table.

The existing `WFHType` entity gets a `policy_id`.

## 9.1 Entity

```java
@Entity
@Table(
    name = "wfh_types",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_wfh_type_policy_name",
            columnNames = {"policy_id", "name"}
        )
    }
)
public class WFHType {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    private String name;

    private String description;

    private int totalDays;

    @Enumerated(EnumType.STRING)
    private DisbursalFrequency disbursalFrequency;
}
```

## 9.2 Important business rule

Same WFH type name can exist across different policies.

Example:

```text
Policy A
  Regular WFH = 12 days

Policy B
  Regular WFH = 8 days
```

But duplicate names inside one policy are not allowed.

```text
UNIQUE(policy_id, name)
```

## 9.3 Disbursal configuration

There is NO separate WFH-disbursal-schedule table.

The WFHType itself contains:

- totalDays
- disbursalFrequency

## 9.4 APIs

```http
POST /policies/{policyId}/wfh-types
GET  /policies/{policyId}/wfh-types
```

Request:

```json
{
  "name": "Regular WFH",
  "description": "Regular work from home entitlement",
  "totalDays": 12,
  "disbursalFrequency": "MONTHLY"
}
```

Again, `policyId` comes from the URL.

Existing individual APIs can remain:

```http
GET    /wfh-types/{id}
PUT    /wfh-types/{id}
DELETE /wfh-types/{id}
```

Modification/deletion should respect policy status.

---

# 10. Employee Lifecycle

One lifecycle configuration per policy.

Current agreed fields:

- probation period
- notice period

Store values in DAYS unless the business requirement changes.

## 10.1 Entity

```java
@Entity
@Table(
    name = "policy_employee_lifecycle",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_employee_lifecycle",
            columnNames = "policy_id"
        )
    }
)
public class PolicyEmployeeLifecycle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "policy_id",
        nullable = false,
        unique = true
    )
    private Policy policy;

    private Integer probationPeriodDays;

    private Integer noticePeriodDays;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 10.2 APIs

```http
PUT /policies/{policyId}/employee-lifecycle
GET /policies/{policyId}/employee-lifecycle
```

Request:

```json
{
  "probationPeriodDays": 90,
  "noticePeriodDays": 30
}
```

Only DRAFT policies can be modified.

---

# 11. Miscellaneous

Miscellaneous is intentionally kept small in Phase 1.

Probation and notice period are NOT stored here because they already belong to Employee Lifecycle.

Overtime is NOT stored here because it is Phase 2.

For the currently agreed UI requirement, miscellaneous contains Dress Code.

## 11.1 Entity

If Dress Code is a fixed set of choices:

```java
public enum DressCode {
    FORMAL,
    BUSINESS_CASUAL,
    CASUAL,
    NO_RESTRICTION
}
```

Entity:

```java
@Entity
@Table(
    name = "policy_miscellaneous",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_miscellaneous",
            columnNames = "policy_id"
        )
    }
)
public class PolicyMiscellaneous {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "policy_id",
        nullable = false,
        unique = true
    )
    private Policy policy;

    @Enumerated(EnumType.STRING)
    private DressCode dressCode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 11.2 APIs

```http
PUT /policies/{policyId}/miscellaneous
GET /policies/{policyId}/miscellaneous
```

Request:

```json
{
  "dressCode": "FORMAL"
}
```

Do not add arbitrary miscellaneous fields unless they are explicitly required.

---

# 12. Policy Management APIs

These are required to complete Phase 1.

## 12.1 Create Policy

```http
POST /policies
```

Request:

```json
{
  "name": "India Standard Employee Policy",
  "description": "Standard policy for India employees",
  "effectiveFrom": "2026-10-01"
}
```

Creates:

```text
version = 1
status = DRAFT
```

---

## 12.2 Update Draft Policy

```http
PUT /policies/{policyId}
```

Expected to update basic policy information such as:

```json
{
  "name": "India Standard Employee Policy",
  "description": "Updated description",
  "effectiveFrom": "2026-10-01"
}
```

Only DRAFT policies can be updated.

---

## 12.3 Get Policy

```http
GET /policies/{policyId}
```

---

## 12.4 List Policies

```http
GET /policies
```

Should support useful filtering/pagination if required by the existing project conventions.

---

# 13. Policy Validation

Before publishing, the policy should be validated.

Endpoint:

```http
GET /policies/{policyId}/validation
```

Example response:

```json
{
  "valid": false,
  "errors": [
    "Work schedule is not configured",
    "No leave types configured"
  ]
}
```

Validation should check that required Phase 1 policy configuration exists and is internally valid.

The exact list of mandatory sections should follow the final product requirement.

---

# 14. Publish Policy

Endpoint:

```http
POST /policies/{policyId}/publish
```

Flow:

```text
DRAFT
  |
  | validate
  v
ACTIVE
```

Publishing should fail if required configuration is missing/invalid.

After activation, policy configuration must be treated as immutable.

---

# 15. Policy Version Creation

Endpoint:

```http
POST /policies/{policyId}/new-version
```

Example:

```text
Existing:
P1
Policy A
Version 1
ACTIVE

Create new version:

P2
Policy A
Version 2
DRAFT
```

The new version must be a new policy row/new policy ID.

The old version must remain unchanged.

The new version gets its own configuration records.

---

# 16. Group → Policy Assignment

Groups already exist.

Add a `policy_id` relationship to the group.

Conceptually:

```text
Group
  |
  └── policy_id → Policy
```

## 16.1 Assign policy

```http
PUT /groups/{groupId}/policy
```

Request:

```json
{
  "policyId": "..."
}
```

Rules:

- Assigned policy must exist.
- Assigned policy must be ACTIVE.
- A group has exactly one currently assigned policy.
- Multiple groups can reference the same policy.

## 16.2 Get group's policy

```http
GET /groups/{groupId}/policy
```

## 16.3 Get groups using a policy

```http
GET /policies/{policyId}/groups
```

---

# 17. Complete Phase 1 API Contract

## Policy

```http
POST /policies
GET  /policies
GET  /policies/{policyId}
PUT  /policies/{policyId}

GET  /policies/{policyId}/validation
POST /policies/{policyId}/publish
POST /policies/{policyId}/new-version
```

## Work Schedule

```http
PUT /policies/{policyId}/work-schedule
GET /policies/{policyId}/work-schedule
```

## Attendance

```http
PUT /policies/{policyId}/attendance
GET /policies/{policyId}/attendance
```

## Leave Types

```http
POST /policies/{policyId}/leave-types
GET  /policies/{policyId}/leave-types

GET    /leave-types/{id}
PUT    /leave-types/{id}
DELETE /leave-types/{id}
```

## WFH Types

```http
POST /policies/{policyId}/wfh-types
GET  /policies/{policyId}/wfh-types

GET    /wfh-types/{id}
PUT    /wfh-types/{id}
DELETE /wfh-types/{id}
```

## Employee Lifecycle

```http
PUT /policies/{policyId}/employee-lifecycle
GET /policies/{policyId}/employee-lifecycle
```

## Miscellaneous

```http
PUT /policies/{policyId}/miscellaneous
GET /policies/{policyId}/miscellaneous
```

## Group Assignment

```http
PUT /groups/{groupId}/policy
GET /groups/{groupId}/policy
GET /policies/{policyId}/groups
```

---

# 18. Overall Data Model

```text
                         ┌──────────────────┐
                         │     Policy       │
                         │──────────────────│
                         │ id               │
                         │ name             │
                         │ version          │
                         │ status           │
                         │ effectiveFrom    │
                         │ effectiveTo      │
                         └────────┬─────────┘
                                  │
             ┌────────────────────┼─────────────────────┐
             │                    │                     │
             v                    v                     v
   ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
   │ Work Schedule   │  │ Attendance Rules │  │ Employee        │
   │                 │  │                  │  │ Lifecycle       │
   └─────────────────┘  └──────────────────┘  └─────────────────┘
             │                    │                     │
             └────────────────────┼─────────────────────┘
                                  │
                     ┌────────────┴────────────┐
                     │                         │
                     v                         v
             ┌───────────────┐         ┌───────────────┐
             │ Leave Types[] │         │ WFH Types[]   │
             └───────────────┘         └───────────────┘

                                  Policy
                                    ^
                                    |
                                  Group
                                    ^
                                    |
                                Employee
```

---

# 19. Important Things NOT To Implement

Do NOT introduce the following in Phase 1:

### Separate policy version table

Do not create:

```text
policy
policy_version
```

Use one `policies` table with `version`.

### Global Leave Type master

Do not create a global leave-type catalog.

Leave types belong to policies.

### Global WFH Type master

Do not create a global WFH-type catalog.

WFH types belong to policies.

### Separate Leave Disbursal Schedule

Do not create:

```text
leave_disbursal_schedule
```

Disbursal frequency is already part of `LeaveType`.

### Separate WFH Disbursal Schedule

Do not create:

```text
wfh_disbursal_schedule
```

Disbursal frequency is already part of `WFHType`.

### Policy Working Day table

Do not create:

```text
policy_working_days
```

Working days are stored as a PostgreSQL array on `policy_work_schedules`.

### Overtime

Do not implement in Phase 1.

Overtime is Phase 2.

### Holiday Calendar

Do not implement in the current Phase 1 policy configuration unless separately requested.

---

# 20. Policy Lifecycle

The intended lifecycle is:

```text
                    ┌─────────────┐
                    │    DRAFT    │
                    └──────┬──────┘
                           │
                      configure
                           │
                           v
                    ┌─────────────┐
                    │    DRAFT    │
                    └──────┬──────┘
                           │
                        publish
                           │
                           v
                    ┌─────────────┐
                    │    ACTIVE   │
                    └──────┬──────┘
                           │
                  no direct modification
                           │
                           v
                 Create New Version
                           │
                           v
                    ┌─────────────┐
                    │    DRAFT    │
                    │ Version N+1 │
                    └─────────────┘
```

An ACTIVE policy should never be modified in place.

---

# 21. Example Complete Flow

## Step 1 — Create policy

```http
POST /policies
```

Result:

```text
Policy P1
Version 1
DRAFT
```

## Step 2 — Configure work schedule

```http
PUT /policies/P1/work-schedule
```

## Step 3 — Configure attendance

```http
PUT /policies/P1/attendance
```

## Step 4 — Add leave types

```http
POST /policies/P1/leave-types
```

For example:

```text
Casual Leave
Sick Leave
Earned Leave
```

## Step 5 — Add WFH types

```http
POST /policies/P1/wfh-types
```

For example:

```text
Regular WFH
Emergency WFH
```

## Step 6 — Configure employee lifecycle

```http
PUT /policies/P1/employee-lifecycle
```

## Step 7 — Configure miscellaneous

```http
PUT /policies/P1/miscellaneous
```

## Step 8 — Validate

```http
GET /policies/P1/validation
```

If valid:

```text
valid = true
```

## Step 9 — Publish

```http
POST /policies/P1/publish
```

P1 becomes:

```text
ACTIVE
```

## Step 10 — Assign to group

```http
PUT /groups/G1/policy
```

```json
{
  "policyId": "P1"
}
```

Now:

```text
Employee
   ↓
Group G1
   ↓
Policy P1
```

---

# 22. Policy Change Flow

Suppose P1 is active and the company wants to change Casual Leave from 12 → 15 days.

DO NOT modify P1.

Instead:

```http
POST /policies/P1/new-version
```

Create:

```text
P2
Policy A
Version 2
DRAFT
```

Configure:

```text
P2
 └── Casual Leave = 15
```

Validate and publish P2.

Then assign P2 to the required groups.

P1 remains unchanged for historical/audit purposes.

---

# 23. Employee Balance Rule

Policy changes must NOT rewrite existing employee balances.

Example:

```text
Employee E1
Group G1
Policy P1

P1 Casual Leave = 12 days
```

If P2 later changes Casual Leave to 15:

```text
P1 → 12
P2 → 15
```

Do not update E1's existing historical balance simply because P2 exists.

Future entitlement/disbursal should follow the applicable policy.

---

# 24. Implementation Verification Checklist

Use this checklist when reviewing the implementation.

## Policy

- [ ] Policy entity exists.
- [ ] Version exists on policy.
- [ ] Status exists.
- [ ] Effective dates exist.
- [ ] DRAFT/ACTIVE lifecycle is enforced.
- [ ] Create Policy API exists.
- [ ] Update Policy API exists.
- [ ] Get/List Policy APIs exist.
- [ ] New Version API exists.
- [ ] Publish API exists.
- [ ] Validation API exists.

## Work Schedule

- [ ] One work schedule per policy.
- [ ] `policy_id` is unique.
- [ ] Check-in time exists.
- [ ] Check-out time exists.
- [ ] Minimum working hours exists.
- [ ] Working days stored as PostgreSQL array.
- [ ] No separate working-day table.
- [ ] GET/PUT APIs exist.

## Attendance

- [ ] One attendance rule per policy.
- [ ] Late mark threshold exists.
- [ ] Consecutive late days exists.
- [ ] Auto-mark absent hours exists.
- [ ] GET/PUT APIs exist.

## Leave Types

- [ ] Existing LeaveType reused.
- [ ] `policy_id` added.
- [ ] `(policy_id, name)` unique.
- [ ] totalDays exists.
- [ ] carryForward exists.
- [ ] disbursalFrequency exists.
- [ ] No separate leave-rule table.
- [ ] No separate leave-disbursal schedule.
- [ ] Create/List policy-scoped APIs exist.
- [ ] Existing individual APIs remain compatible.
- [ ] ACTIVE policy cannot be modified.

## WFH Types

- [ ] Existing WFHType reused.
- [ ] `policy_id` added.
- [ ] `(policy_id, name)` unique.
- [ ] totalDays exists.
- [ ] disbursalFrequency exists.
- [ ] No separate WFH-rule table.
- [ ] No separate WFH-disbursal schedule.
- [ ] Create/List policy-scoped APIs exist.
- [ ] Existing individual APIs remain compatible.
- [ ] ACTIVE policy cannot be modified.

## Employee Lifecycle

- [ ] One lifecycle config per policy.
- [ ] Probation period exists.
- [ ] Notice period exists.
- [ ] Values represented in days unless business requirements say otherwise.
- [ ] GET/PUT APIs exist.

## Miscellaneous

- [ ] One miscellaneous config per policy.
- [ ] Dress Code exists if required by UI.
- [ ] No probation/notice duplication.
- [ ] No overtime in Phase 1.
- [ ] GET/PUT APIs exist.

## Group Assignment

- [ ] Group has `policy_id`.
- [ ] One current policy per group.
- [ ] Multiple groups can use same policy.
- [ ] Only ACTIVE policies can be assigned.
- [ ] Assign policy API exists.
- [ ] Get group policy API exists.
- [ ] Get policy groups API exists.

## Versioning

- [ ] New version creates a new policy row.
- [ ] New version gets a new policy ID.
- [ ] Version number increments.
- [ ] Previous version remains unchanged.
- [ ] New version starts as DRAFT.
- [ ] Existing balances are not rewritten.
- [ ] ACTIVE policies are immutable.

---

# 25. Phase 2

Explicitly deferred:

```text
- Overtime
- Holiday Calendar
- Other advanced policy rules as required
```

Do not implement these as part of the current Phase 1 contract unless requirements change.
