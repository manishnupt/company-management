package com.hrms.company_management.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.service.NoticeService;
import com.hrms.company_management.utility.NoticeAccessGuard;

/** Employee view: only notices that are published and not expired, for the caller's tenant. */
@RestController
@RequestMapping("/notices")
@CrossOrigin(origins = "*")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeAccessGuard accessGuard;

    public NoticeController(NoticeService noticeService, NoticeAccessGuard accessGuard) {
        this.noticeService = noticeService;
        this.accessGuard = accessGuard;
    }

    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getVisibleNotices(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        accessGuard.requireEmployee(authorization);
        return ResponseEntity.ok(noticeService.getVisibleNotices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getVisibleNotice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id) {
        accessGuard.requireEmployee(authorization);
        return ResponseEntity.ok(noticeService.getVisibleNotice(id));
    }
}
