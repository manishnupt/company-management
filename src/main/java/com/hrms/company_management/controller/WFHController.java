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
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/wfh")
@CrossOrigin(origins = "*")
@Log4j2
public class WFHController {
    @Autowired
    private WFHService wfhService;

    // Create
    @PostMapping("/apply")
    public ResponseEntity<WFHType> createWFHType(@RequestBody WFHType wfhType) {
        log.info("createWFHType called with wfhType:{}", wfhType);
        WFHType created = wfhService.createWFHType(wfhType);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // get all
    @GetMapping("/list")
    public ResponseEntity<java.util.List<WFHType>> getAllWFHTypes() {
        log.info("getAllWFHTypes called");
        java.util.List<WFHType> wfhTypes = wfhService.getAllWFHTypes();
        return ResponseEntity.ok(wfhTypes);
    }

    // Read
    @GetMapping("/{id}")
    public ResponseEntity<WFHType> getWFHTypeById(@PathVariable String id) {
        log.info("getWFHTypeById called with id:{}", id);
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
        log.info("updateWFHType called with id:{}, wfhType:{}", id, wfhType);
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
        log.info("deleteWFHType called with id:{}", id);
        boolean deleted = wfhService.deleteWFHType(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/schedule/{type}")
    public ResponseEntity<List<WFHType>> getSchedule(@PathVariable String type) {
        log.info("getSchedule called with type:{}", type);
        try {
            List<WFHType> wfhType = wfhService.getByType(type);
            return ResponseEntity.ok(wfhType);
        } catch (Exception e) {
            log.error("Error in getSchedule for type:{}", type, e);
            return ResponseEntity.ok(null);
        }
    }

}
