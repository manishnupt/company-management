package com.hrms.company_management.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hrms.company_management.dto.WFHRequest;
import com.hrms.company_management.dto.WFHResponse;
import com.hrms.company_management.service.WFHService;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/wfh-types")
@CrossOrigin(origins = "*")
@Log4j2
public class WFHController {
    @Autowired
    private WFHService wfhService;

    // Create
    @PostMapping
    public ResponseEntity<WFHResponse> createWFHType(@RequestBody WFHRequest wfhRequest) {
        log.info("Received request to create WFH type: {}", wfhRequest);
        WFHResponse created = wfhService.createWFHType(wfhRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // get all
    @GetMapping()
    public ResponseEntity<List<WFHResponse>> getAllWFHTypes() {
        log.info("Received request to fetch all WFH types");
        List<WFHResponse> wfhTypes = wfhService.getAllWFHTypes();
        return ResponseEntity.ok(wfhTypes);
    }

    // Read
    @GetMapping("/{id}")
    public ResponseEntity<WFHResponse> getWFHTypeById(@PathVariable String id) {
        log.info("Received request to fetch WFH type by id: {}", id);
        WFHResponse wfhType = wfhService.getWFHTypeById(id);
        if (wfhType != null) {
            return ResponseEntity.ok(wfhType);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<WFHResponse> updateWFHType(@PathVariable String id, @RequestBody WFHRequest wfhRequest) {
        log.info("Received request to update WFH type with id: {}", id);
        WFHResponse updated = wfhService.updateWFHType(id, wfhRequest);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Delete
    @PostMapping("/delete")
    public ResponseEntity<Void> deleteWFHType(@RequestParam String id) {
        log.info("Received request to delete WFH type with id: {}", id);
        boolean deleted = wfhService.deleteWFHType(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/schedule/{type}")
    public ResponseEntity<List<WFHResponse>> getSchedule(@PathVariable String type) {
        log.info("Received request to fetch WFH schedule by type: {}", type);
        try {
            List<WFHResponse> wfhType = wfhService.getByType(type);
            return ResponseEntity.ok(wfhType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

}
