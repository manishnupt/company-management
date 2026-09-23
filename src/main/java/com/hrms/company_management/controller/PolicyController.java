package com.hrms.company_management.controller;

import com.hrms.company_management.dto.AssignGroupsRequest;
import com.hrms.company_management.dto.AttendanceRuleRequest;
import com.hrms.company_management.dto.AttendanceRuleResponse;
import com.hrms.company_management.dto.CreatePolicyRequest;
import com.hrms.company_management.dto.EmployeeLifecycleRequest;
import com.hrms.company_management.dto.EmployeeLifecycleResponse;
import com.hrms.company_management.dto.GroupResponse;
import com.hrms.company_management.dto.MiscellaneousRequest;
import com.hrms.company_management.dto.MiscellaneousResponse;
import com.hrms.company_management.dto.PolicyResponse;
import com.hrms.company_management.dto.PolicyValidationResponse;
import com.hrms.company_management.dto.UpdatePolicyRequest;
import com.hrms.company_management.dto.WorkScheduleRequest;
import com.hrms.company_management.dto.WorkScheduleResponse;
import com.hrms.company_management.service.PolicyService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/policies")
@CrossOrigin(origins = "*")
@Log4j2
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    // Create
    @PostMapping
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        log.info("Received request to create policy: {}", request);
        PolicyResponse created = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get all
    @GetMapping
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        log.info("Received request to fetch all policies");
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    // Get by id
    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable UUID policyId) {
        log.info("Received request to fetch policy: {}", policyId);
        return ResponseEntity.ok(policyService.getPolicyById(policyId));
    }

    // Update policy details (DRAFT only)
    @PutMapping("/{policyId}")
    public ResponseEntity<PolicyResponse> updatePolicy(@PathVariable UUID policyId,
                                                       @Valid @RequestBody UpdatePolicyRequest request) {
        log.info("Received request to update policy {}: {}", policyId, request);
        return ResponseEntity.ok(policyService.updatePolicy(policyId, request));
    }

    // Create a new DRAFT version copied from an existing version
    @PostMapping("/{policyId}/new-version")
    public ResponseEntity<PolicyResponse> createNewVersion(@PathVariable UUID policyId) {
        log.info("Received request to create new version from policy: {}", policyId);
        PolicyResponse created = policyService.createNewVersion(policyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Check whether a policy is ready to publish
    @GetMapping("/{policyId}/validation")
    public ResponseEntity<PolicyValidationResponse> validatePolicy(@PathVariable UUID policyId) {
        log.info("Received request to validate policy {}", policyId);
        return ResponseEntity.ok(policyService.validatePolicy(policyId));
    }

    // Publish a DRAFT version (DRAFT -> ACTIVE); earlier versions stay as they are
    @PostMapping("/{policyId}/publish")
    public ResponseEntity<PolicyResponse> publish(@PathVariable UUID policyId) {
        log.info("Received request to publish policy: {}", policyId);
        return ResponseEntity.ok(policyService.publish(policyId));
    }

    // Move groups onto a published version
    @PutMapping("/{policyId}/groups")
    public ResponseEntity<List<GroupResponse>> assignGroups(@PathVariable UUID policyId,
                                                            @Valid @RequestBody AssignGroupsRequest request) {
        log.info("Received request to assign groups to policy {}: {}", policyId, request);
        return ResponseEntity.ok(policyService.assignGroups(policyId, request.getGroupIds()));
    }

    // Groups on a policy version
    @GetMapping("/{policyId}/groups")
    public ResponseEntity<List<GroupResponse>> getGroups(@PathVariable UUID policyId) {
        log.info("Received request to fetch groups for policy {}", policyId);
        return ResponseEntity.ok(policyService.getGroups(policyId));
    }

    // Create or replace work schedule
    @PutMapping("/{policyId}/work-schedule")
    public ResponseEntity<WorkScheduleResponse> upsertWorkSchedule(@PathVariable UUID policyId,
                                                                   @Valid @RequestBody WorkScheduleRequest request) {
        log.info("Received request to upsert work schedule for policy {}: {}", policyId, request);
        return ResponseEntity.ok(policyService.upsertWorkSchedule(policyId, request));
    }

    // Get work schedule
    @GetMapping("/{policyId}/work-schedule")
    public ResponseEntity<WorkScheduleResponse> getWorkSchedule(@PathVariable UUID policyId) {
        log.info("Received request to fetch work schedule for policy {}", policyId);
        return ResponseEntity.ok(policyService.getWorkSchedule(policyId));
    }

    // Create or replace attendance rules
    @PutMapping("/{policyId}/attendance")
    public ResponseEntity<AttendanceRuleResponse> upsertAttendanceRule(@PathVariable UUID policyId,
                                                                       @Valid @RequestBody AttendanceRuleRequest request) {
        log.info("Received request to upsert attendance rules for policy {}: {}", policyId, request);
        return ResponseEntity.ok(policyService.upsertAttendanceRule(policyId, request));
    }

    // Get attendance rules
    @GetMapping("/{policyId}/attendance")
    public ResponseEntity<AttendanceRuleResponse> getAttendanceRule(@PathVariable UUID policyId) {
        log.info("Received request to fetch attendance rules for policy {}", policyId);
        return ResponseEntity.ok(policyService.getAttendanceRule(policyId));
    }

    // Create or replace employee lifecycle
    @PutMapping("/{policyId}/employee-lifecycle")
    public ResponseEntity<EmployeeLifecycleResponse> upsertEmployeeLifecycle(@PathVariable UUID policyId,
                                                                             @Valid @RequestBody EmployeeLifecycleRequest request) {
        log.info("Received request to upsert employee lifecycle for policy {}: {}", policyId, request);
        return ResponseEntity.ok(policyService.upsertEmployeeLifecycle(policyId, request));
    }

    // Get employee lifecycle
    @GetMapping("/{policyId}/employee-lifecycle")
    public ResponseEntity<EmployeeLifecycleResponse> getEmployeeLifecycle(@PathVariable UUID policyId) {
        log.info("Received request to fetch employee lifecycle for policy {}", policyId);
        return ResponseEntity.ok(policyService.getEmployeeLifecycle(policyId));
    }

    // Create or replace miscellaneous
    @PutMapping("/{policyId}/miscellaneous")
    public ResponseEntity<MiscellaneousResponse> upsertMiscellaneous(@PathVariable UUID policyId,
                                                                     @Valid @RequestBody MiscellaneousRequest request) {
        log.info("Received request to upsert miscellaneous for policy {}: {}", policyId, request);
        return ResponseEntity.ok(policyService.upsertMiscellaneous(policyId, request));
    }

    // Get miscellaneous
    @GetMapping("/{policyId}/miscellaneous")
    public ResponseEntity<MiscellaneousResponse> getMiscellaneous(@PathVariable UUID policyId) {
        log.info("Received request to fetch miscellaneous for policy {}", policyId);
        return ResponseEntity.ok(policyService.getMiscellaneous(policyId));
    }
}
