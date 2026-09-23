# Policy Phase 1: Gaps and Deviations

These items **exist in the code but deviate from `hrms_policy_phase1_contract.md`, or are buggy or risky**. Items that are missing entirely are listed in `policy_phase1_not_implemented.md`.

**Validated on:** 2026-09-23 (branch `feature/policy_damn`, including uncommitted changes)

Severity:
- **High** breaks the contract or a core rule.
- **Medium** is wrong behaviour in some cases.
- **Low** is cleanup or hardening.

---

## High

### G-1 Legacy `POST /wfh-types` creates a WFH type with no policy
- **Where:** `WFHController.java:27`, `WFHService.java:36`
- **Problem:** `WFHRequest` has no `policyId`, and the service never sets one. `WFHType.policy` is `optional = false` / `nullable = false`, so every call fails with a Hibernate or DB error, which surfaces as a 500. Because it bypasses `requireDraft`, it also breaks the rule that WFH types are policy-owned.
- **Fix:** Remove the endpoint, or have it return 410 or redirect to the policy-scoped create.

### G-2 Policy-scoped create and list routes don't match the contract
- **Where:** `LeavesPolicyController.java:71`, `WFHController.java:90`, `WFHController.java:104`
- **Problem:** Actual routes vs the contract:

  | Actual | Contract |
  |---|---|
  | `POST /leave-types/{policyId}` | `POST /policies/{policyId}/leave-types` |
  | `POST /wfh-types/{policyId}` | `POST /policies/{policyId}/wfh-types` |
  | `GET /wfh-types/policies/{policyId}/wfh-types` (class prefix added accidentally) | `GET /policies/{policyId}/wfh-types` |

  A frontend built against the contract gets 404s or 405s.
- **Fix:** Move these handlers to the contract paths. The simplest option is to put them in `PolicyController`, or give the handler an absolute mapping in a controller without the class-level prefix.

### G-3 Duplicate leave or WFH type names within a policy are allowed
- **Where:** `LeavePolicyService.create/update`, `WFHService.create/updateWFHType`
- **Problem:** The code has no `(policy_id, name)` check. If the DB does enforce the constraint, the violation is caught by `LeavesPolicyController`'s generic `catch (Exception)` and returned as **200 with a `null` body**, so the client thinks it succeeded. On the WFH side it becomes a 500.
- **Fix:** Add `existsByPolicy_IdAndNameIgnoreCase` checks that return 409, add `@UniqueConstraint` to both entities, and ship the DB constraint.

### G-4 Leave and WFH create DTO validation never runs
- **Where:** `LeavesPolicyController.java:72`, `WFHController.java:91`
- **Problem:** `@RequestBody` has no `@Valid`, so `@NotBlank name`, `@Min(0) totalDays` and `@NotNull disbursalFrequency` on `CreateLeaveTypeRequest` / `CreateWfhTypeRequest` are ignored. Blank names and negative days get saved.
- **Fix:** Add `@Valid`. Also validate `PUT /leave-types/{id}`, which takes the raw `LeaveType` entity, and `PUT /wfh-types/{id}`, where `WFHRequest` has no constraints.

### G-5 Group-assignment API shape differs from the contract ✅ Resolved 2026-09-24 (contract routes added in `GroupPolicyController`)
- **Where:** `PolicyController.java:90`
- **Problem:** The contract specifies `PUT /groups/{groupId}/policy {policyId}`, one group at a time. The implementation is `PUT /policies/{policyId}/groups {groupIds[]}`. The behaviour matches (ACTIVE-only, one policy per group), but the route and payload don't. Also, `GET /groups/{groupId}/policy` doesn't exist, so the current assignment can't be read from the group side (see the not-implemented file, #3–4).
- **Fix:** Add the contract routes; the bulk route can stay alongside them.

## Medium

### G-6 Errors are swallowed and returned as 200 in the leave and WFH controllers
- **Where:** `LeavesPolicyController.java:28-111`, `WFHController.java:81-112`
- **Problem:** These cases return 200 with `null`, `false` or an empty list instead of 4xx:
  - Leave not found on `GET /leave-types/{id}`
  - Leave not found on `PUT /leave-types/{id}` (the service returns `null`)
  - A malformed `policyId` (the `UUID.fromString` `IllegalArgumentException`)
  - DB errors
  - `GET /policies/{policyId}/leave-types` for a policy that doesn't exist, which returns `[]` (it never calls `getPolicyEntityById`, unlike the WFH version)
  - An invalid `schedule/{type}`
- **Fix:** Remove the `try/catch (Exception)` blocks, throw `ResponseStatusException` (404/400), and let Spring map them.

### G-7 Race between draft edits and publish
- **Where:** `LeavePolicyService`, `WFHService` and all `upsert*` methods in `PolicyService`
- **Problem:** `requireDraft` reads the status without a lock, then saves. A concurrent `publish` can commit between the check and the write, which changes an ACTIVE policy's configuration and breaks the immutability rule. `LeavePolicyService` / `WFHService` mutations aren't `@Transactional` either.
- **Fix:** In card and type mutations, load the policy with `findByIdForUpdate` (the same lock `publish` takes) inside a `@Transactional` method.

### G-8 Older ACTIVE versions stay ACTIVE and assignable after a new version is published
- **Where:** `PolicyService.publish` (`PolicyService.java:318`)
- **Problem:** After v2 is published, v1 stays `ACTIVE`, so groups can still be assigned to v1 and there's no signal that it's superseded. The contract doesn't say v1 must change, but it defines `INACTIVE` / `EXPIRED` without ever using them.
- **Fix:** Product decision needed: either mark the previous ACTIVE version `INACTIVE` on publish (optionally setting its `effectiveTo`), or block assigning a superseded version.

### G-9 Legacy leave and WFH read endpoints ignore policy and status
- **Where:** `GET /leave-types`, `GET /leave-types/schedule/{type}`, `GET /wfh-types`, `GET /wfh-types/schedule/{type}`
- **Problem:** They return types from **every** policy and version, including DRAFTs and superseded versions. Each new version copies all types, so every "Casual Leave" is listed once per version. `WFHResponse` doesn't even include `policyId`, so callers can't tell the copies apart.
- **Fix:** Scope these by policy or group, or at least filter to `status = ACTIVE` and include `policyId` in responses.

### G-10 Nothing makes disbursal use the employee's group policy
- **Where:** Contract §3.1 ("Future leave/WFH disbursal should use the policy applicable to the employee's group"), §23
- **Problem:** No code path resolves Employee → Group → Policy → leave/WFH types. If a downstream disbursal job uses the `schedule/{type}` endpoints (G-9), it credits employees with the types of **all** policies. Where employee balances live isn't visible from this service, so this couldn't be verified end to end.
- **Fix:** Expose a resolver such as `GET /groups/{groupId}/policy` plus that policy's types, and make disbursal consume it.

### G-11 `WFHService.getAllWFHTypes` / `WFHMapper` map through ModelMapper on a lazy `policy`
- **Where:** `WFHMapper.convertToWfhTypeResponse`
- **Problem:** `WfhTypeResponse.policyId` relies on ModelMapper's implicit `policy.id → policyId` match. It works today, but it's fragile. The WFH `create` path isn't `@Transactional`, and the mapper touches a LAZY proxy. `LeavePolicyService` maps explicitly and is safer.
- **Fix:** Map `policyId` explicitly, as `LeavePolicyService.convertToResponseDto` does.

### G-12 Validation completeness is a product decision still open
- **Where:** `PolicyService.validate` (`PolicyService.java:263`)
- **Problem:** Missing WFH types and missing Miscellaneous are **warnings**, so a policy with neither can be published. The contract (§13) says "exact list of mandatory sections should follow the final product requirement". Validation also doesn't re-check that leave and WFH `totalDays >= 0`, which matters because of G-4.
- **Fix:** Confirm the mandatory sections with product. Add checks on leave and WFH values.

## Low

### G-13 Policy audit fields are only partly maintained
- `createdBy` and `updatedBy` are never set.
- `updatedAt` is set by hand in `updatePolicy` / `publish` only; `Policy` has no `@PreUpdate`.
- The card tables do have `@PreUpdate`.

### G-14 Work schedule mapping differs from the contract's literal spec
- **Where:** `entity/PolicyWorkSchedule.java`
- **Details:**
  - `workingDays` is `Set<DayOfWeek>` with `@Enumerated`, not `String[]`, and isn't marked `nullable = false`.
  - `minimumWorkingHours` has no `precision = 5, scale = 2`.
  - There's no named `uk_policy_work_schedule` (only `unique = true` on the join column).
- **Impact:** Type-safe and fine in principle. Check at runtime that Hibernate 6.6 reads and writes `Set<DayOfWeek>` correctly against a Postgres `varchar[]` column; if it doesn't, add `@JdbcTypeCode(SqlTypes.ARRAY)`.

### G-15 `createNewVersion` / `publish` modify legacy source rows
- **Where:** `PolicyService.java:108-110`, `329-330`
- **Problem:** For rows created before versioning, `rootPolicyId` is backfilled on the **source** policy, which may be ACTIVE. It's harmless metadata, but it technically breaks "old version remains unchanged".
- **Fix:** Backfill `root_policy_id` in the migration instead.

### G-16 Nothing prevents two separate policies with the same name
- **Where:** `PolicyService.createPolicy`
- **Problem:** Two separate `POST /policies` calls with the same name both create a `version = 1` row under different roots. That violates the contract's `uk_policy_name_version`. `updatePolicy` can also rename a draft v2 so its name differs from v1.
- **Fix:** Decide whether name is unique per root or globally, then enforce it.

### G-17 `RoleGroup.policy` is nullable
- **Problem:** The contract says "each group has exactly one currently assigned policy". New groups (`POST /admin/group`) start with no policy. That's acceptable during rollout, but validation or reporting should flag groups without a policy.

### G-18 Leftover dead code
- `entity/WFHPolicy.java` is an empty class.
- `utility/PolicyHelper.extractPolicyData` is from the old key/value model.
- `LeavesPolicyController` imports `LeaveType` entities into responses; the rest of the module uses DTOs.
