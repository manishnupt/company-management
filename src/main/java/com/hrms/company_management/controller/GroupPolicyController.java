package com.hrms.company_management.controller;

import com.hrms.company_management.dto.AssignPolicyRequest;
import com.hrms.company_management.dto.PolicyResponse;
import com.hrms.company_management.service.PolicyService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
@CrossOrigin(origins = "*")
@Log4j2
public class GroupPolicyController {

    @Autowired
    private PolicyService policyService;

    // Move a group onto a published (ACTIVE) policy version
    @PutMapping("/{groupId}/policy")
    public ResponseEntity<PolicyResponse> assignPolicy(@PathVariable Long groupId,
                                                       @Valid @RequestBody AssignPolicyRequest request) {
        log.info("Received request to assign policy {} to group {}", request.getPolicyId(), groupId);
        return ResponseEntity.ok(policyService.assignPolicyToGroup(groupId, request.getPolicyId()));
    }

    // Policy version the group currently follows
    @GetMapping("/{groupId}/policy")
    public ResponseEntity<PolicyResponse> getPolicy(@PathVariable Long groupId) {
        log.info("Received request to fetch policy for group {}", groupId);
        return ResponseEntity.ok(policyService.getGroupPolicy(groupId));
    }
}
