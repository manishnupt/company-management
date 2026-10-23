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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.company_management.dto.NoticeRequest;
import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.entity.NoticeStatus;
import com.hrms.company_management.service.NoticeService;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

/** Notice management. Same base path as the old AdminController notice endpoints. */
@RestController
@RequestMapping("/admin/notice")
@CrossOrigin(origins = "*")
@Log4j2
public class NoticeAdminController {

    private final NoticeService noticeService;

    public NoticeAdminController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @RequestBody NoticeRequest noticeRequest) {
        log.info("Received request to create notice");
        return ResponseEntity.status(HttpStatus.CREATED).body(noticeService.createNotice(noticeRequest));
    }

    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getAllNotices(
            @RequestParam(required = false) NoticeStatus status) {
        log.info("Received request to fetch all notices, status filter: {}", status);
        return ResponseEntity.ok(noticeService.getAllNotices(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNotice(
            @PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNotice(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody NoticeRequest noticeRequest) {
        log.info("Received request to update notice {}", id);
        return ResponseEntity.ok(noticeService.updateNotice(id, noticeRequest));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<NoticeResponse> publishNotice(
            @PathVariable Long id) {
        log.info("Received request to publish notice {}", id);
        return ResponseEntity.ok(noticeService.publishNotice(id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<NoticeResponse> archiveNotice(
            @PathVariable Long id) {
        log.info("Received request to archive notice {}", id);
        return ResponseEntity.ok(noticeService.archiveNotice(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long id) {
        log.info("Received request to delete notice {}", id);
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
