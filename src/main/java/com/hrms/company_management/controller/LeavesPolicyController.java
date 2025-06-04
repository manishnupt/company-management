package com.hrms.company_management.controller;


import com.hrms.company_management.dto.BulkLeaveAssignmentDto;
import com.hrms.company_management.dto.LeaveTypeDto;
import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.service.LeavePolicyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/leave-types")
@CrossOrigin(origins="*")
public class LeavesPolicyController {

    @Autowired
    private LeavePolicyService service;

    @GetMapping
    public ResponseEntity<List<LeaveType>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveType> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<LeaveType> getByName(@PathVariable String name) {
        return ResponseEntity.ok(service.getByName(name));
    }

    @PostMapping
    public ResponseEntity<LeaveType> create(@Valid @RequestBody LeaveTypeDto leaveTypeDto) {
        return ResponseEntity.ok(service.create(leaveTypeDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveType> update(@PathVariable String id,
                                            @Valid @RequestBody LeaveTypeDto leaveTypeDto) {
        return ResponseEntity.ok(service.update(id, leaveTypeDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @PostMapping("/assign-to-all")
    public ResponseEntity<String> assignLeaveToAllEmployees(@Valid @RequestBody BulkLeaveAssignmentDto assignmentDto) {
        return service.assignLeaveToAllEmployees(assignmentDto);
    }

    @PostMapping("/assign-to-employee/{employeeId}/{leaveTypeId}/{days}")
    public ResponseEntity<String> assignLeaveToEmployee(@PathVariable String employeeId,
                                                        @PathVariable String leaveTypeId,
                                                        @PathVariable int days) {
        return service.assignLeaveToEmployee(employeeId, leaveTypeId, days);
    }
}
