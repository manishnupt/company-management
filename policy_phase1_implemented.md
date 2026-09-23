# Policy Phase 1: What's Implemented

This file lists what the current code on `feature/policy_damn`, including uncommitted changes, delivers against `hrms_policy_phase1_contract.md`.

- **Validated on:** 2026-09-23
- **Build:** `mvn -o compile` on JDK 17 succeeds. The repo has no tests.
- **Schema:** `spring.jpa.hibernate.ddl-auto=none`, and the repo has no migrations. Table and constraint claims below are based on the JPA mappings only.

Related files:
- `policy_phase1_not_implemented.md` lists items that are missing entirely.
- `policy_phase1_gaps.md` lists items that exist but deviate from the contract or are risky.

---

## 1. Policy entity (contract §3, §4)

| Contract item | Status | Where |
|---|---|---|
| Single `policies` table (no `policy_version` table) | ✅ | `entity/Policy.java` |
| UUID id, name (150, not null), description (TEXT) | ✅ | `Policy.java:20-28` |
| `version` (not null, default 1) | ✅ | `Policy.java:30-31` |
| `status` enum `DRAFT/ACTIVE/INACTIVE/EXPIRED` | ✅ | `utility/PolicyStatus.java` |
| `effectiveFrom`, `effectiveTo` | ✅ | `Policy.java:41-43` |
| `createdAt` (not null, not updatable), `createdBy`, `updatedAt`, `updatedBy` columns | ✅ (columns exist; see gaps for population) | `Policy.java:45-52` |
| Extra: `rootPolicyId` links all versions of one policy | ➕ addition | `Policy.java:33-35` |

## 2. Policy management APIs (contract §12)

| API | Status | Where |
|---|---|---|
| `POST /policies`: creates version 1, `DRAFT` | ✅ | `PolicyController.java:38`, `PolicyService.java:82` |
| `GET /policies` | ✅ (no filtering or pagination) | `PolicyController.java:46` |
| `GET /policies/{policyId}` (404 when missing) | ✅ | `PolicyController.java:53`, `PolicyService.java:219` |
| `PUT /policies/{policyId}`: DRAFT only, 409 otherwise, rejects `effectiveTo < effectiveFrom` | ✅ | `PolicyController.java:60`, `PolicyService.java:226` |
| Request validation (`@NotBlank name`, `@NotNull effectiveFrom`) | ✅ | `dto/CreatePolicyRequest.java`, `dto/UpdatePolicyRequest.java` |

## 3. Validation (contract §13)

`GET /policies/{policyId}/validation` (`PolicyService.java:263`) returns `{policyId, status, valid, errors[], warnings[]}`.

It reports these as **errors**, which block publishing:
- `effectiveFrom` not set
- `effectiveTo` before `effectiveFrom`
- Work schedule missing
- `minimumWorkingHours` longer than the shift (overnight shifts are handled)
- Attendance rules missing
- Employee lifecycle missing
- No leave types

It reports these as **warnings**, which don't block publishing:
- `effectiveFrom` in the past
- Miscellaneous missing
- No WFH types

## 4. Publish (contract §14)

`POST /policies/{policyId}/publish` (`PolicyService.java:318`):
- ✅ Requires `DRAFT` (409 otherwise).
- ✅ Runs validation and fails with 400 listing the errors.
- ✅ Moves `DRAFT → ACTIVE` and leaves other versions untouched.
- ✅ Takes a pessimistic lock on the root row, so it serialises with `new-version`.

## 5. Versioning (contract §3.1, §15, §20, §22)

`POST /policies/{policyId}/new-version` (`PolicyService.java:98`):
- ✅ Creates a new row with a new UUID.
- ✅ `version = max(version for root) + 1`
- ✅ The new version starts as `DRAFT` and shares `rootPolicyId`.
- ✅ The source version's configuration is left unchanged.
- ✅ Deep-copies every card into new rows owned by the new version (`copyCards`, `PolicyService.java:139`): work schedule, attendance, lifecycle, miscellaneous, leave types and WFH types (leave and WFH types get new ids).
- ✅ Refuses to branch from a `DRAFT` (409) and allows only one open `DRAFT` per root (409). A row lock on the root guards against races.

Immutability of ACTIVE policies:
- ✅ `requireDraft` (`PolicyService.java:249`) guards policy update, all four card upserts, leave-type create/update/delete and WFH-type create/update/delete.
- ✅ The error message tells the caller to create a new version.

## 6. Work schedule (contract §6)

- ✅ Entity `policy_work_schedules`, `@OneToOne` with `policy_id` `unique = true`, not null (`entity/PolicyWorkSchedule.java`)
- ✅ `checkInTime`, `checkOutTime`, `minimumWorkingHours` (not null)
- ✅ `working_days` mapped as `varchar[]` on the same table, with no separate working-day table. It's typed as `Set<DayOfWeek>`, not `String[]`.
- ✅ `PUT /policies/{policyId}/work-schedule` upsert (`PolicyController.java:105`, `PolicyService.java:383`)
  - Policy must exist (404) and be DRAFT (409)
  - `workingDays` `@NotEmpty`
  - `minimumWorkingHours > 0`
  - Check-in and check-out can't be equal
  - `HH:mm` format
  - The rule that minimum hours can't exceed the shift is enforced during validation.

## 7. Attendance rules (contract §7)

- ✅ Entity `policy_attendance_rules` with `uk_policy_attendance_rule(policy_id)` (`entity/PolicyAttendanceRule.java`)
- ✅ `lateMarkThresholdMinutes`, `consecutiveLateDaysAllowed`, `autoMarkAbsentAfterHours` (precision 5, scale 2)
- ✅ `PUT /policies/{policyId}/attendance` upsert
  - DRAFT only
  - Integers `@Min(0)`, hours `> 0` (`dto/AttendanceRuleRequest.java`)

## 8. Leave types (contract §8)

- ✅ Existing `LeaveType` reused, with `policy_id` added (`@ManyToOne`, not null) (`entity/LeaveType.java`)
- ✅ `totalDays`, `carryForward`, `disbursalFrequency`, `description` on the leave type. There is no separate rule or disbursal table.
- ✅ `GET /policies/{policyId}/leave-types` (`LeavesPolicyController.java:60`)
- ✅ Create is policy-scoped and takes `policyId` from the path. The route differs from the contract; see the gaps file.
- ✅ `GET /leave-types/{id}`, `PUT /leave-types/{id}` and `DELETE /leave-types/{id}` still exist. PUT and DELETE enforce DRAFT (`LeavePolicyService.java:88,103`).

## 9. WFH types (contract §9)

- ✅ Existing `WFHType` reused, with `policy_id` added (`@ManyToOne`, not null) (`entity/WFHType.java`)
- ✅ `totalDays`, `disbursalFrequency`, `description`. There is no separate rule or disbursal table.
- ✅ Policy-scoped create and list exist. The routes differ from the contract; see the gaps file.
- ✅ `GET /wfh-types/{id}` and `PUT /wfh-types/{id}` exist, and PUT and delete enforce DRAFT (`WFHService.java:66,82`).

## 10. Employee lifecycle (contract §10)

- ✅ Entity `policy_employee_lifecycle` with `uk_policy_employee_lifecycle(policy_id)`
- ✅ `probationPeriodDays`, `noticePeriodDays` (days, `@Min(0)`, not null)
- ✅ `PUT` (DRAFT only) and `GET /policies/{policyId}/employee-lifecycle` (`PolicyController.java:121,129`)

## 11. Miscellaneous (contract §11)

- ✅ Entity `policy_miscellaneous` with `uk_policy_miscellaneous(policy_id)`
- ✅ Contains only `dressCode`: no probation, notice or overtime duplication. It's a free-text `String`, not an enum; see the gaps file.
- ✅ `PUT` (DRAFT only) and `GET /policies/{policyId}/miscellaneous` (`PolicyController.java:136,144`)

## 12. Group → policy assignment (contract §16)

- ✅ `RoleGroup` has a nullable `policy_id` `@ManyToOne` (`entity/RoleGroup.java:32-38`), so each group has at most one current policy.
- ✅ Many groups can reference the same policy.
- ✅ Only `ACTIVE` policies can be assigned (409 otherwise), and unknown group ids return 404 (`PolicyService.java:344`).
- ✅ Assignment exists as `PUT /policies/{policyId}/groups` with `{ "groupIds": [...] }`. It's a bulk variant; the contract's group-side route is missing (see the not-implemented file).
- ✅ `GET /policies/{policyId}/groups` (`PolicyController.java:98`)

## 13. Phase 1 exclusions respected (contract §19, §25)

- ✅ No `policy_version` table
- ✅ No global leave or WFH master
- ✅ No leave or WFH disbursal schedule tables
- ✅ No `policy_working_days` table
- ✅ No overtime
- ✅ No holiday calendar inside the policy
- ✅ The old generic `PolicyAttribute` key/value model has been removed.

## 14. Employee balance rule (contract §23)

- ✅ This is met by default: nothing in this service reads or writes employee balances, so publishing or versioning a policy can't rewrite them.
