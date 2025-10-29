package com.hrms.company_management.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.company_management.entity.WFHType;
import com.hrms.company_management.service.WFHService;

@RestController
@RequestMapping("/wfh")
@CrossOrigin(origins = "*")
public class WFHController {
    @Autowired
    private WFHService wfhService;

    // Create
    @PostMapping("/apply")
    public ResponseEntity<WFHType> createWFHType(@RequestBody WFHType wfhType) {
        WFHType created = wfhService.createWFHType(wfhType);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // get all
    @GetMapping("/list")
    public ResponseEntity<java.util.List<WFHType>> getAllWFHTypes() {
        java.util.List<WFHType> wfhTypes = wfhService.getAllWFHTypes();
        return ResponseEntity.ok(wfhTypes);
    }

    // Read
    @GetMapping("/{id}")
    public ResponseEntity<WFHType> getWFHTypeById(@PathVariable String id) {
        WFHType wfhType = wfhService.getWFHTypeById(id);
        if (wfhType != null) {
            return ResponseEntity.ok(wfhType);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Update
    @PostMapping("/update")
    public ResponseEntity<WFHType> updateWFHType(@RequestParam String id, @RequestBody WFHType wfhType) {
        WFHType updated = wfhService.updateWFHType(id, wfhType);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Delete
    @PostMapping("/delete")
    public ResponseEntity<Void> deleteWFHType(@RequestParam String id) {
        boolean deleted = wfhService.deleteWFHType(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/schedule/{type}")
    public ResponseEntity<List<WFHType>> getSchedule(@PathVariable String type) {
        try {
            List<WFHType> wfhType = wfhService.getByType(type);
            return ResponseEntity.ok(wfhType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }

}
