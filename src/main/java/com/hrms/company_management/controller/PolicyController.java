package com.hrms.company_management.controller;

import com.hrms.company_management.dto.AttendanceRuleRequest;
import com.hrms.company_management.dto.AttendanceRuleResponse;
import com.hrms.company_management.dto.CreatePolicyRequest;
import com.hrms.company_management.dto.EmployeeLifecycleRequest;
import com.hrms.company_management.dto.EmployeeLifecycleResponse;
import com.hrms.company_management.dto.MiscellaneousRequest;
import com.hrms.company_management.dto.MiscellaneousResponse;
import com.hrms.company_management.dto.PolicyResponse;
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

    // Create or replace work schedule
    @PutMapping("/{policyId}/work-schedule")
    public ResponseEntity<WorkScheduleResponse> upsertWorkSchedule(@PathVariable UUID policyId,
                                                                   @Valid @RequestBody WorkScheduleRequest request) {
        log.info("Received request to upsert work schedule for policy {}: {}", policyId, request);
        return ResponseEntity.ok(policyService.upsertWorkSchedule(policyId, request));
    }

    // Create or replace attendance rules
    @PutMapping("/{policyId}/attendance")
    public ResponseEntity<AttendanceRuleResponse> upsertAttendanceRule(@PathVariable UUID policyId,
                                                                       @Valid @RequestBody AttendanceRuleRequest request) {
        log.info("Received request to upsert attendance rules for policy {}: {}", policyId, request);
        return ResponseEntity.ok(policyService.upsertAttendanceRule(policyId, request));
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
