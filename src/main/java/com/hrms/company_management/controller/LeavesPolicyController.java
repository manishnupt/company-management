package com.hrms.company_management.controller;


import com.hrms.company_management.dto.CreateLeaveTypeRequest;
import com.hrms.company_management.dto.LeaveTypeResponse;
import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.service.LeavePolicyService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin(origins="*")
@Log4j2
public class LeavesPolicyController {

    @Autowired
    private LeavePolicyService service;

    @GetMapping("/leave-types")
    public ResponseEntity<List<LeaveType>> getAll() {
        log.info("Received request to fetch all leave types");
        try {
            return ResponseEntity.ok(service.getAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/leave-types/{id}")
    public ResponseEntity<LeaveType> getById(@PathVariable String id) {
        log.info("Received request to fetch leave type by id: {}", id);
        try {
            LeaveType leaveType = service.getById(id);
            return ResponseEntity.ok(leaveType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @GetMapping("/leave-types/schedule/{type}")
    public ResponseEntity<List<LeaveType>> getByType(@PathVariable String type) {
        log.info("Received request to fetch leave types by disbursal type: {}", type);
        try {
            List<LeaveType> leaveType = service.getByType(type);
            return ResponseEntity.ok(leaveType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @GetMapping("/policies/{policyId}/leave-types")
    public ResponseEntity<List<LeaveTypeResponse>> getByPolicyId(@PathVariable String policyId) {
        log.info("Received request to fetch leave types by policy id: {}", policyId);
        try {
            return ResponseEntity.ok(service.getByPolicyId(policyId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @PostMapping("/leave-types/{policyId}")
    public ResponseEntity<LeaveTypeResponse> create(@Valid @RequestBody CreateLeaveTypeRequest request,@PathVariable String policyId) {
        log.info("Received request to create leave type: {}", request);
        try {
            LeaveTypeResponse created = service.create(request,policyId);
            return ResponseEntity.ok(created);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @PutMapping("/leave-types/{id}")
    public ResponseEntity<LeaveType> update(@PathVariable String id,
                                            @RequestBody LeaveType leaveType) {
        log.info("Received request to update leave type with id: {}", id);
        try {
            LeaveType updated = service.update(id, leaveType);
            return ResponseEntity.ok(updated);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @DeleteMapping("/leave-types/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        log.info("Received request to delete leave type with id: {}", id);
        try {
            boolean result = service.delete(id);
            return ResponseEntity.ok(result);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }
}

