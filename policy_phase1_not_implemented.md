# Policy Phase 1: Not Implemented

These items from `hrms_policy_phase1_contract.md` have **no implementation at all** in the current code. Items that exist but deviate are listed in `policy_phase1_gaps.md`.

**Validated on:** 2026-09-23 (branch `feature/policy_damn`, including uncommitted changes)

---

## 1. Missing endpoints

| # | Contract endpoint | Contract § | Notes / suggested fix |
|---|---|---|---|
| 1 | ~~`GET /policies/{policyId}/work-schedule`~~ | §6.3, §17 | ✅ **Done (2026-09-24).** `PolicyController.getWorkSchedule` → `PolicyService.getWorkSchedule`. Returns 404 if the policy or schedule is missing. |
| 2 | ~~`GET /policies/{policyId}/attendance`~~ | §7.2, §17 | ✅ **Done (2026-09-24).** `PolicyController.getAttendanceRule` → `PolicyService.getAttendanceRule`. Returns 404 if the policy or rules are missing. |
| 3 | ~~`PUT /groups/{groupId}/policy` with body `{ "policyId": "..." }`~~ | §16.1, §17 | ✅ **Done (2026-09-24).** `GroupPolicyController.assignPolicy` → `PolicyService.assignPolicyToGroup`. Returns 404 for an unknown group or policy, 409 if the policy isn't ACTIVE, and 400 if `policyId` is missing. The response is the assigned `PolicyResponse`. The bulk `PUT /policies/{policyId}/groups` route is kept. |
| 4 | ~~`GET /groups/{groupId}/policy`~~ | §16.2, §17 | ✅ **Done (2026-09-24).** `GroupPolicyController.getPolicy` → `PolicyService.getGroupPolicy`. Returns `PolicyResponse`, or 404 if the group is unknown or has no policy assigned. |
| 5 | `POST /policies/{policyId}/wfh-types` at the contract path | §9.4, §17 | Nothing is mapped at this path. Create is mapped at `POST /wfh-types/{policyId}` instead. |
| 6 | `GET /policies/{policyId}/wfh-types` at the contract path | §9.4, §17 | Nothing is mapped at this path. `WFHController` has class-level `@RequestMapping("/wfh-types")`, so the method actually resolves to **`GET /wfh-types/policies/{policyId}/wfh-types`** (`WFHController.java:104`). A client calling the contract URL gets a 404. |
| 7 | `POST /policies/{policyId}/leave-types` at the contract path | §8.4, §17 | Nothing is mapped at this path. Create is mapped at `POST /leave-types/{policyId}` instead (`LeavesPolicyController.java:71`). |
| 8 | `DELETE /wfh-types/{id}` | §9.4, §17 | Not present. Delete is `POST /wfh-types/delete?id=...` (`WFHController.java:67`). |

## 2. Missing model elements

| # | Contract item | Contract § | Notes |
|---|---|---|---|
| 9 | `uk_policy_name_version` unique constraint on `policies(name, version)` | §4 | Not declared on `Policy`, and the service doesn't check it. With `ddl-auto=none` the DB might have it, but nothing in the repo shows that. |
| 10 | `uk_leave_type_policy_name` unique constraint on `leave_types(policy_id, name)` | §8.1, §8.2 | Not declared on `LeaveType`. Neither create nor update checks for duplicate names in the same policy. |
| 11 | `uk_wfh_type_policy_name` unique constraint on `wfh_types(policy_id, name)` | §9.1, §9.2 | Not declared on `WFHType`. Neither create nor update checks for duplicates. |
| 12 | `DressCode` enum (`FORMAL`, `BUSINESS_CASUAL`, `CASUAL`, `NO_RESTRICTION`) | §11.1 | Not present. `dressCode` is a free-text `String(500)`. The contract makes the enum conditional ("if Dress Code is a fixed set of choices"), so product needs to confirm. |
| 13 | Populating `createdBy` / `updatedBy` | §3.1 (audit), §4 | The columns exist but nothing ever sets them. |
| 14 | DB migration or DDL for the new tables and columns | (implied) | The repo has no Flyway, Liquibase or SQL scripts, and `ddl-auto=none`. Every tenant DB needs these created by hand: `policies.root_policy_id`, `role_group.policy_id`, `leave_types.policy_id`, `wfh_types.policy_id`, the four `policy_*` card tables and all the unique constraints. |

## 3. Missing policy lifecycle behaviour

| # | Contract item | Contract § | Notes |
|---|---|---|---|
| 15 | Any transition to `INACTIVE` / `EXPIRED` | §4 | These enum values exist but nothing ever sets them. There's no deactivate endpoint and no job that expires a policy after `effectiveTo`. An older ACTIVE version stays ACTIVE forever after a newer one is published, so it can still be assigned to groups. The contract doesn't define an endpoint for this; product needs to decide. |
| 16 | Resolving the policy that applies to an employee (Employee → Group → Policy) for future disbursal | §2.1, §3.1, §23 | This service has no API that answers "which policy applies to employee X". Nothing in the disbursal paths (`/leave-types/schedule/{type}`, `/wfh-types/schedule/{type}`) is policy- or group-aware. See gaps G-10. |
| 17 | Filtering and pagination on `GET /policies` | §12.4 | Optional per the contract ("if required by existing project conventions"). No filters by status, name or `rootPolicyId`, and no paging. |

## 4. Tests

| # | Item | Notes |
|---|---|---|
| 18 | Tests for any policy workflow | `src/test` has no tests. None of the §24 checklist is covered by automated tests. |
