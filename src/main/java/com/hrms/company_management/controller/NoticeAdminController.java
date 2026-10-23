package com.hrms.company_management.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.company_management.dto.NoticeCaller;
import com.hrms.company_management.dto.NoticeRequest;
import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.entity.NoticeStatus;
import com.hrms.company_management.service.NoticeService;
import com.hrms.company_management.utility.NoticeAccessGuard;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;

/** Notice management for admins. Same base path as the old AdminController notice endpoints. */
@RestController
@RequestMapping("/admin/notice")
@CrossOrigin(origins = "*")
@Log4j2
public class NoticeAdminController {

    private final NoticeService noticeService;
    private final NoticeAccessGuard accessGuard;

    public NoticeAdminController(NoticeService noticeService, NoticeAccessGuard accessGuard) {
        this.noticeService = noticeService;
        this.accessGuard = accessGuard;
    }

    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody NoticeRequest noticeRequest) {
        NoticeCaller caller = accessGuard.requireAdmin(authorization);
        log.info("Received request to create notice from {}", caller.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(noticeService.createNotice(noticeRequest, caller));
    }

    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getAllNotices(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(required = false) NoticeStatus status) {
        accessGuard.requireAdmin(authorization);
        log.info("Received request to fetch all notices, status filter: {}", status);
        return ResponseEntity.ok(noticeService.getAllNotices(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNotice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id) {
        accessGuard.requireAdmin(authorization);
        return ResponseEntity.ok(noticeService.getNotice(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticeResponse> updateNotice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody NoticeRequest noticeRequest) {
        NoticeCaller caller = accessGuard.requireAdmin(authorization);
        log.info("Received request to update notice {}", id);
        return ResponseEntity.ok(noticeService.updateNotice(id, noticeRequest, caller));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<NoticeResponse> publishNotice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id) {
        NoticeCaller caller = accessGuard.requireAdmin(authorization);
        log.info("Received request to publish notice {}", id);
        return ResponseEntity.ok(noticeService.publishNotice(id, caller));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<NoticeResponse> archiveNotice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id) {
        NoticeCaller caller = accessGuard.requireAdmin(authorization);
        log.info("Received request to archive notice {}", id);
        return ResponseEntity.ok(noticeService.archiveNotice(id, caller));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id) {
        NoticeCaller caller = accessGuard.requireAdmin(authorization);
        log.info("Received request to delete notice {}", id);
        noticeService.deleteNotice(id, caller);
        return ResponseEntity.noContent().build();
    }
}
