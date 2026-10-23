package com.hrms.company_management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.company_management.dto.NoticeRequest;
import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.entity.NoticeStatus;
import com.hrms.company_management.service.NoticeService;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

/**
 * All notice endpoints.
 * /admin/notice manages notices in any state; /notices returns only published, unexpired notices.
 */
@RestController
@CrossOrigin(origins = "*")
@Log4j2
public class NoticeController {

    private static final String ADMIN_BASE = "/admin/notice";
    private static final String VIEW_BASE = "/notices";

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // ---------- Management ----------

    @PostMapping(ADMIN_BASE)
    public ResponseEntity<NoticeResponse> createNotice(@Valid @RequestBody NoticeRequest noticeRequest) {
        log.info("Received request to create notice");
        return ResponseEntity.status(HttpStatus.CREATED).body(noticeService.createNotice(noticeRequest));
    }

    @GetMapping(ADMIN_BASE)
    public ResponseEntity<List<NoticeResponse>> getAllNotices(@RequestParam(required = false) NoticeStatus status) {
        log.info("Received request to fetch all notices, status filter: {}", status);
        return ResponseEntity.ok(noticeService.getAllNotices(status));
    }

    @GetMapping(ADMIN_BASE + "/{id}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNotice(id));
    }

    @PutMapping(ADMIN_BASE + "/{id}")
    public ResponseEntity<NoticeResponse> updateNotice(@PathVariable Long id,
                                                       @Valid @RequestBody NoticeRequest noticeRequest) {
        log.info("Received request to update notice {}", id);
        return ResponseEntity.ok(noticeService.updateNotice(id, noticeRequest));
    }

    @PostMapping(ADMIN_BASE + "/{id}/publish")
    public ResponseEntity<NoticeResponse> publishNotice(@PathVariable Long id) {
        log.info("Received request to publish notice {}", id);
        return ResponseEntity.ok(noticeService.publishNotice(id));
    }

    @PostMapping(ADMIN_BASE + "/{id}/archive")
    public ResponseEntity<NoticeResponse> archiveNotice(@PathVariable Long id) {
        log.info("Received request to archive notice {}", id);
        return ResponseEntity.ok(noticeService.archiveNotice(id));
    }

    @DeleteMapping(ADMIN_BASE + "/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        log.info("Received request to delete notice {}", id);
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Published notices ----------

    @GetMapping(VIEW_BASE)
    public ResponseEntity<List<NoticeResponse>> getVisibleNotices() {
        return ResponseEntity.ok(noticeService.getVisibleNotices());
    }

    @GetMapping(VIEW_BASE + "/{id}")
    public ResponseEntity<NoticeResponse> getVisibleNotice(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getVisibleNotice(id));
    }
}
