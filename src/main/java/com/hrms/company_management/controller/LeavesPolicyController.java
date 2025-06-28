package com.hrms.company_management.controller;


import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.service.LeavePolicyService;
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
        try {
            return ResponseEntity.ok(service.getAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveType> getById(@PathVariable String id) {
        try {
            LeaveType leaveType = service.getById(id);
            return ResponseEntity.ok(leaveType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @GetMapping("/schedule/{type}")
    public ResponseEntity<List<LeaveType>> getByType(@PathVariable String type) {
        try {
            List<LeaveType> leaveType = service.getByType(type);
            return ResponseEntity.ok(leaveType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }



    @PostMapping
    public ResponseEntity<LeaveType> create(@RequestBody LeaveType leaveType) {
        try {
            LeaveType created = service.create(leaveType);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveType> update(@PathVariable String id,
                                            @RequestBody LeaveType leaveType) {
        try {
            LeaveType updated = service.update(id, leaveType);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        try {
            boolean result = service.delete(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }
}

