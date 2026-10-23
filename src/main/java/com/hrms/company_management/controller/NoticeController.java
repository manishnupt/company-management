package com.hrms.company_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.company_management.dto.NoticeResponse;
import com.hrms.company_management.service.NoticeService;

/** Employee view: only notices that are published and not expired */
@RestController
@RequestMapping("/notices")
@CrossOrigin(origins = "*")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getVisibleNotices() {
        return ResponseEntity.ok(noticeService.getVisibleNotices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getVisibleNotice(
            @PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getVisibleNotice(id));
    }
}
