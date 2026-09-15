package com.hrms.company_management.controller;


import com.hrms.company_management.entity.LeaveType;
import com.hrms.company_management.service.LeavePolicyService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/leave-types")
@CrossOrigin(origins="*")
@Log4j2
public class LeavesPolicyController {

    @Autowired
    private LeavePolicyService service;

    @GetMapping
    public ResponseEntity<List<LeaveType>> getAll() {
        log.info("getAll called");
        try {
            return ResponseEntity.ok(service.getAll());
        } catch (Exception e) {
            log.error("Error in getAll", e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveType> getById(@PathVariable String id) {
        log.info("getById called with id:{}", id);
        try {
            LeaveType leaveType = service.getById(id);
            return ResponseEntity.ok(leaveType);
        } catch (Exception e) {
            log.error("Error in getById for id:{}", id, e);
            return ResponseEntity.ok(null);
        }
    }

    @GetMapping("/schedule/{type}")
    public ResponseEntity<List<LeaveType>> getByType(@PathVariable String type) {
        log.info("getByType called with type:{}", type);
        try {
            List<LeaveType> leaveType = service.getByType(type);
            return ResponseEntity.ok(leaveType);
        } catch (Exception e) {
            log.error("Error in getByType for type:{}", type, e);
            return ResponseEntity.ok(null);
        }
    }



    @PostMapping
    public ResponseEntity<LeaveType> create(@RequestBody LeaveType leaveType) {
        log.info("create called with leaveType:{}", leaveType);
        try {
            LeaveType created = service.create(leaveType);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error in create for leaveType:{}", leaveType, e);
            return ResponseEntity.ok(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveType> update(@PathVariable String id,
                                            @RequestBody LeaveType leaveType) {
        log.info("update called with id:{}, leaveType:{}", id, leaveType);
        try {
            LeaveType updated = service.update(id, leaveType);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error in update for id:{}", id, e);
            return ResponseEntity.ok(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        log.info("delete called with id:{}", id);
        try {
            boolean result = service.delete(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in delete for id:{}", id, e);
            return ResponseEntity.ok(false);
        }
    }
}

