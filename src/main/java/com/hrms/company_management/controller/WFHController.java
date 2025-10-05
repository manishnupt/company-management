package com.hrms.company_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.company_management.entity.WFHType;
import com.hrms.company_management.service.WFHService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/wfh")
@CrossOrigin(origins = "*")
public class WFHController {
    @Autowired
    private WFHService wfhService;

    // Define your endpoints here crud
    // Create
    @PostMapping
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
    @GetMapping
    public ResponseEntity<WFHType> getWFHTypeById(@RequestParam String id) {
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

}
